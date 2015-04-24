package org.kaaproject.kaa.examples.powerplant.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    private static final ConcurrentMap<Integer, SortedSet<DataPoint>> voltageSamplesMap = new ConcurrentHashMap<>();

    // TODO: remove this hardcode data;
    static {
        long time = new Date().getTime();
        voltageSamplesMap.putIfAbsent(1, new TreeSet<DataPoint>(DataPoint.TS_COMPARATOR));
        voltageSamplesMap.putIfAbsent(2, new TreeSet<DataPoint>(DataPoint.TS_COMPARATOR));
        voltageSamplesMap.putIfAbsent(3, new TreeSet<DataPoint>(DataPoint.TS_COMPARATOR));
        voltageSamplesMap.putIfAbsent(4, new TreeSet<DataPoint>(DataPoint.TS_COMPARATOR));
        voltageSamplesMap.get(1).add(new DataPoint(1, 5.2, time));
        voltageSamplesMap.get(2).add(new DataPoint(2, 3.7, time));
        voltageSamplesMap.get(3).add(new DataPoint(3, 2.2, time));
        voltageSamplesMap.get(4).add(new DataPoint(4, 4.1, time));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DataPoint> getData(@DefaultValue("0") @QueryParam("from") long time) {
        List<Integer> keyList = sortKeyList();
        LOG.trace("Sorted list {}", keyList);

        List<DataPoint> result = new ArrayList<DataPoint>();
        for (Integer key : keyList) {
            LOG.trace("Filtering {} samples using ts {}", key, time);
            SortedSet<DataPoint> samples = voltageSamplesMap.get(key).tailSet(new DataPoint(key, 0.0, time));
            result.addAll(samples);
        }

        return result;
    }

    @GET
    @Path("latest")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DataPoint> getLatest() {
        List<Integer> keyList = sortKeyList();

        List<DataPoint> result = new ArrayList<DataPoint>();
        for (Integer key : keyList) {
            result.add(voltageSamplesMap.get(key).last());
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

        long ts = new Date().getTime();
        for (VoltageSample sample : report.getSamples()) {
            LOG.trace("processing {}", sample);
            sample.setTimestamp(ts);
            Integer panelId = sample.getPanelId();
            SortedSet<DataPoint> sampleSet = voltageSamplesMap.get(panelId);
            if (sampleSet == null) {
                LOG.trace("[{}] no samples for this panel yet", panelId);
                sampleSet = Collections.synchronizedSortedSet(new TreeSet<DataPoint>(DataPoint.TS_COMPARATOR));
                SortedSet<DataPoint> curSet = voltageSamplesMap.putIfAbsent(panelId, sampleSet);
                if (curSet != null) {
                    sampleSet = curSet;
                }
            }
            sampleSet.add(convert(sample));
            if (sampleSet.size() == MAX_SAMPLES) {
                DataPoint old = sampleSet.first();
                sampleSet.remove(old);
                LOG.trace("[{}] removing old data {}", panelId, old);
            }
        }

        return Response.noContent().build();
    }

    private static DataPoint convert(VoltageSample sample) {
        return new DataPoint(sample.getPanelId(), sample.getVoltage(), sample.getTimestamp());
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
