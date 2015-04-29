package org.kaaproject.kaa.examples.powerplant.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.kaaproject.kaa.examples.powerplant.VoltageReport;
import org.kaaproject.kaa.examples.powerplant.VoltageSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("data")
public class DashboardResource {
    private static final Logger LOG = LoggerFactory.getLogger(DashboardResource.class);

    private static final int MAX_SAMPLES = 100;
    private static final Map<Integer, SortedSet<DataPoint>> voltageSamplesMap = new HashMap<>();
    private static final Object updateLock = new Object();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DataPoint> getData(@DefaultValue("0") @QueryParam("from") long time) {
        List<Integer> keyList = sortKeyList();
        LOG.trace("Sorted list {}", keyList);

        List<DataPoint> result = new ArrayList<DataPoint>();
        synchronized (updateLock) {
            for (Integer key : keyList) {
                LOG.trace("Filtering {} samples using ts {}", key, time);
                SortedSet<DataPoint> samples = voltageSamplesMap.get(key).tailSet(new DataPoint(key, 0.0f, time));
                result.addAll(samples);
            }
        }

        return result;
    }

    @GET
    @Path("latest")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DataPoint> getLatest() {
        List<Integer> keyList = sortKeyList();

        List<DataPoint> result = new ArrayList<DataPoint>();
        synchronized (updateLock) {
            for (Integer key : keyList) {
                result.add(voltageSamplesMap.get(key).last());
            }
        }
        return result;
    }

    protected List<Integer> sortKeyList() {
        List<Integer> keyList = new ArrayList<Integer>(voltageSamplesMap.keySet());
        Collections.sort(keyList);
        LOG.trace("Sorted list {}", keyList);
        return keyList;
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response putValue(String reportBody) {
        VoltageReport report = decode(reportBody);
        if (report == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        long ts = System.currentTimeMillis();
        synchronized (updateLock) {
            for (VoltageSample sample : report.getSamples()) {
                LOG.trace("processing {}", sample);
                Integer panelId = sample.getPanelId();
                SortedSet<DataPoint> sampleSet = voltageSamplesMap.get(panelId);
                if (sampleSet == null) {
                    LOG.trace("[{}] no samples for this panel yet", panelId);
                    sampleSet = Collections.synchronizedSortedSet(new TreeSet<DataPoint>(DataPoint.TS_COMPARATOR));
                    SortedSet<DataPoint> curSet = voltageSamplesMap.put(panelId, sampleSet);
                    if (curSet != null) {
                        sampleSet = curSet;
                    }
                }
                sampleSet.add(convert(sample, ts));
                if (sampleSet.size() == MAX_SAMPLES) {
                    DataPoint old = sampleSet.first();
                    sampleSet.remove(old);
                    LOG.trace("[{}] removing old data {}", panelId, old);
                }
            }
        }
        return Response.noContent().build();
    }

    private static DataPoint convert(VoltageSample sample, long ts) {
        return new DataPoint(sample.getPanelId(), sample.getVoltage().floatValue(), ts);
    }

    private static final DatumReader<VoltageReport> datumReader = new SpecificDatumReader<VoltageReport>(VoltageReport.SCHEMA$);

    private static VoltageReport decode(String reportBody) {
        try {
            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(VoltageReport.SCHEMA$, reportBody);
            VoltageReport report = null;
            report = datumReader.read(report, decoder);
            return report;
        } catch (Exception e) {
            LOG.warn("Failed to decode report source {}", reportBody, e);
            return null;
        }
    }
}
