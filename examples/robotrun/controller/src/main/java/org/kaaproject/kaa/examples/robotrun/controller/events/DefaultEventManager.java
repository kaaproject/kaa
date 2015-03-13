/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.examples.robotrun.controller.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.FetchEventListeners;
import org.kaaproject.kaa.client.event.registration.EndpointOperationResultListener;
import org.kaaproject.kaa.client.event.registration.UserAuthResultListener;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.examples.robotrun.controller.Context;
import org.kaaproject.kaa.examples.robotrun.gen.event.EntityInfo;
import org.kaaproject.kaa.examples.robotrun.gen.event.EntityInfoRequest;
import org.kaaproject.kaa.examples.robotrun.gen.event.EntityInfoResponse;
import org.kaaproject.kaa.examples.robotrun.gen.event.EntityType;
import org.kaaproject.kaa.examples.robotrun.gen.event.ExitFoundNotice;
import org.kaaproject.kaa.examples.robotrun.gen.event.Location;
import org.kaaproject.kaa.examples.robotrun.gen.event.MovementRequest;
import org.kaaproject.kaa.examples.robotrun.gen.event.MovementResponse;
import org.kaaproject.kaa.examples.robotrun.gen.event.RobotRunEventClassFamily;
import org.kaaproject.kaa.examples.robotrun.gen.event.StartRunRequest;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.RobotPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEventManager implements EventManager, RobotRunEventClassFamily.Listener, UserAuthResultListener,
        FetchEventListeners {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventManager.class);

    /** Default processing time out in milliseconds */
    private static final int PROCESSING_TIME_OUT = 3000;

    /** Default user account external UID */
    private static final String USER_EXTERNAL_ID = "robotrun@kaaproject.org";

    /** Default user account access token */
    private static final String USER_ACCESS_TOKEN = "dummy_acess_token";

    private final ExecutorService eventExecutor = Executors.newCachedThreadPool();

    private volatile CountDownLatch moveRequestLatch;
    private volatile String moveRequestId;
    private volatile Cell targetCell;
    private volatile boolean targetCellApproved;

    private boolean moveRequestConflict;
    private volatile Set<Boolean> moveRequestResponses;
    private List<EventListener> listeners = new ArrayList<>();
    private Map<String, RobotPosition> robotPositions = Collections.synchronizedMap(new HashMap<String, RobotPosition>());
    private List<String> attachedEndpoints = new ArrayList<>();
    private List<String> entitiesToDiscover;

    private RobotRunEventClassFamily family;
    private KaaClient client;
    private EntityType entityType;
    private EventManagerDataProvider dataProvider;
    private boolean startRunPending = false;

    public DefaultEventManager(KaaClient client, EntityType entityType, EventManagerDataProvider dataProvider) {
        LOG.debug("Init default event manager.");
        this.entityType = entityType;
        this.client = client;
        this.dataProvider = dataProvider;
        this.family = client.getEventFamilyFactory().getRobotRunEventClassFamily();
        family.addListener(this);
    }

    @Override
    public void init() {
        atachToUser();
    }

    private void atachToUser() {
        client.attachUser(USER_EXTERNAL_ID, USER_ACCESS_TOKEN, this);
    }

    @Override
    public void onAuthResult(UserAttachResponse response) {
        if (response.getResult() == SyncResponseResultType.SUCCESS) {
            fetchEndpointKeys();
        } else {
            for (EventListener listener : listeners) {
                listener.onEventManagerInitFailed("Unable to attach user!");
            }
        }
    }

    @Override
    public void resetEndpoints() {
        fetchEndpointKeys();
    }

    private void fetchEndpointKeys() {
        LOG.info("Fetching endpoint keys...");
        List<String> fqns = Arrays.asList(StartRunRequest.class.getName(), EntityInfoRequest.class.getName(),
                MovementRequest.class.getName());
        client.findEventListeners(fqns, this);
    }

    @Override
    public void onEventListenersReceived(List<String> endpointKeys) {
        this.attachedEndpoints = endpointKeys;
        LOG.info("EventListenersReceived endpointKeys.size [{}], startRunPending [{}]", endpointKeys.size(), startRunPending);
        if (startRunPending) {
            startRunPending = false;
            discoverEntities();
        } else {
            if (entityType == EntityType.DESKTOP) {
                detachEndpoints();
            } else {
                for (EventListener listener : listeners) {
                    listener.onEventManagerInitComplete();
                }
            }
        }
    }

    @Override
    public void onRequestFailed() {
        for (EventListener listener : listeners) {
            listener.onEventManagerInitFailed("Failed to fetch attached endpoint keys!");
        }
    }

    private void detachEndpoints() {
        if (!attachedEndpoints.isEmpty()) {
            final List<String> toDetach = new ArrayList<>(attachedEndpoints);
            for (final String endpointKeyHash : toDetach) {
                EndpointKeyHash endpointKey = new EndpointKeyHash(endpointKeyHash);
                client.detachEndpoint(endpointKey,
                        new EndpointOperationResultListener() {
                            @Override
                            public void sendResponse(String operation, SyncResponseResultType result, Object context) {
                                if (result == SyncResponseResultType.SUCCESS) {
                                    toDetach.remove(endpointKeyHash);
                                    if (toDetach.isEmpty()) {
                                        for (EventListener listener : listeners) {
                                            listener.onEventManagerInitComplete();
                                        }
                                    }
                                } else {
                                    for (EventListener listener : listeners) {
                                        listener.onEventManagerInitFailed("Unable to detach endpoint!");
                                    }
                                }
                            }
                        });
            }
        } else {
            for (EventListener listener : listeners) {
                listener.onEventManagerInitComplete();
            }
        }
    }

    @Override
    public void startRun() {
        startRunPending = true;
        fetchEndpointKeys();
    }

    @Override
    public int getRobotCount() {
        return robotPositions.size();
    }

    @Override
    public Map<String, RobotPosition> getRobotPositions() {
        return robotPositions;
    }

    @Override
    public boolean addListener(EventListener listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean removeListener(EventListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void reportLocation(Cell cell) {
        targetCell = null;
        final EntityInfoResponse response = new EntityInfoResponse();
        response.setEntityInfo(buildEntityInfo(cell));
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                family.sendEventToAll(response);
            }
        });
    }

    @Override
    public void exitFound(Cell cell) {
        final ExitFoundNotice exitFoundNotice = new ExitFoundNotice(cellToLocation(cell));
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                family.sendEventToAll(exitFoundNotice);
            }
        });
    }

    @Override
    public void reset() {
        robotPositions.clear();
        for (EventListener listener : listeners) {
            listener.onRobotLocationChanged(null, null, null);
        }
    }

    @Override
    public void onEvent(StartRunRequest paramStartRunRequest, String sourceEndpointKey) {
        if (checkEventAllowed(paramStartRunRequest, sourceEndpointKey)) {
            LOG.info("Got start run request from [{}]. Start run pending...", sourceEndpointKey);
            startRunPending = true;
            fetchEndpointKeys();
        }
    }

    private void discoverEntities() {
        entitiesToDiscover = new ArrayList<>(this.attachedEndpoints);
        LOG.info("Discovering entities [{}]...", entitiesToDiscover.size());
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                family.sendEventToAll(new EntityInfoRequest());
            }
        });
    }

    private void processStartRun() {
        LOG.info("Processing start run...");
        if (entityType == EntityType.ROBOT) {
            for (EventListener listener : listeners) {
                listener.onStartRun();
            }
        }
        else {
            eventExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    family.sendEventToAll(new StartRunRequest());
                }
            });
        }
    }

    @Override
    public void onEvent(EntityInfoRequest paramEntityInfoRequest, final String sourceEndpointKey) {
        if (checkEventAllowed(paramEntityInfoRequest, sourceEndpointKey)) {
            LOG.debug("Got entity info request from [{}].", sourceEndpointKey);
            final EntityInfoResponse response = new EntityInfoResponse();
            if (entityType == EntityType.ROBOT) {
                response.setEntityInfo(buildEntityInfo(dataProvider.getCurrentCell()));
            } else {
                response.setEntityInfo(buildEntityInfo());
            }
            eventExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    family.sendEvent(response, sourceEndpointKey);
                }
            });
        }
    }

    @Override
    public void onEvent(EntityInfoResponse paramEntityInfoResponse, String sourceEndpointKey) {
        if (checkEventAllowed(paramEntityInfoResponse, sourceEndpointKey)) {
            LOG.debug("Got entity info response from [{}].", sourceEndpointKey);
            EntityInfo entityInfo = paramEntityInfoResponse.getEntityInfo();
            if (EntityType.ROBOT.equals(entityInfo.getEntityType())) {
                Location location = entityInfo.getLocation();
                Cell cell = locationToCell(location);
                if(Boolean.TRUE.equals(entityInfo.getDeadEnd())){
                    LOG.debug("DeadEnd info received for cell {}", cell);
                    cell.markDeadEnd();
                }
                robotPositions.put(sourceEndpointKey, new RobotPosition(cell, entityInfo.getName()));
                for (EventListener listener : listeners) {
                    listener.onRobotLocationChanged(sourceEndpointKey, entityInfo.getName(), cell);
                }
            }
            if (entitiesToDiscover != null && !entitiesToDiscover.isEmpty()) {
                entitiesToDiscover.remove(sourceEndpointKey);
                LOG.info("Discovered entity [{}], entitiesToDiscover.size [{}]", sourceEndpointKey, entitiesToDiscover.size());
                if (entitiesToDiscover.isEmpty()) {
                    LOG.info("All entities discovered, robot count [{}].", getRobotCount());
                    processStartRun();
                }
            }
        }
    }

    @Override
    public void onEvent(MovementRequest paramMovementRequest, final String sourceEndpointKey) {
        if (checkEventAllowed(paramMovementRequest, sourceEndpointKey)) {
            LOG.info("Got movement request from [{}]. Current cell {}, Target cell {}, target allowed {}",
                    sourceEndpointKey, dataProvider.getCurrentCell(), targetCell, targetCellApproved);
            if (entityType == EntityType.ROBOT) {
                final MovementResponse response = new MovementResponse(paramMovementRequest.getRequestId(), true);
                Cell requestCell = locationToCell(paramMovementRequest.getTargetLocation());
                Cell current = dataProvider.getCurrentCell();
                if (current != null && current.equals(requestCell)) {
                    LOG.info("Can't allow move to my current position for request [{}].",
                            paramMovementRequest.getRequestId());
                    response.setAccepted(false);
                } else {
                    if (targetCell != null && targetCell.equals(requestCell)) {
                        if (targetCellApproved) {
                            LOG.info("Can't allow move to my target position for request [{}] due to approval.",
                                    paramMovementRequest.getRequestId());
                            response.setAccepted(false);
                        } else {
                            analyzePriority(paramMovementRequest, sourceEndpointKey, response, "target");
                        }
                    }
                }
                
                if(response.getAccepted() && !requestCell.isDiscovered()){
                    if(Context.isNeighbors(current, requestCell)){
                        LOG.info("Current and request cells are neighbors");
                        Direction borderDirection = Context.getDirection(current, requestCell);
                        if(current.getBorder(borderDirection) == BorderType.UNKNOWN){
                            LOG.info("Can't allow to move to my undiscovered cell neighbor");
                            response.setAccepted(false);
                        }else{
                            LOG.info("Conflict border is already discovered!");
                        }
                    }else if(targetCell != null && Context.isNeighbors(targetCell, requestCell)){
                        LOG.info("Target and request cells are neighbors");
                        Direction borderDirection = Context.getDirection(targetCell, requestCell);
                        if(targetCell.getBorder(borderDirection) == BorderType.UNKNOWN){
                            analyzePriority(paramMovementRequest, sourceEndpointKey, response, "target neighbor");
                        }else{
                            LOG.info("Conflict border is already discovered!");
                        }
                    }
                }
                if (response.getAccepted()) {
                    LOG.info("Allowed to move to [{}] for request [{}].", requestCell,
                            paramMovementRequest.getRequestId());
                }

                eventExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        family.sendEvent(response, sourceEndpointKey);
                    }
                });
            }
        }
    }
    
    private final Object conflictLock = new Object();

    private void analyzePriority(MovementRequest paramMovementRequest, final String sourceEndpointKey,
            final MovementResponse response, String log) {
        synchronized (conflictLock) {
            if (sourceEndpointKey.hashCode() < client.getEndpointKeyHash().hashCode()) {
                LOG.info("Can't allow move to my {} position for request [{}] due to priority.", log,
                        paramMovementRequest.getRequestId());
                response.setAccepted(false);
            } else {
                LOG.info("Skiped my move due to request [{}].",
                        paramMovementRequest.getRequestId());
                moveRequestConflict = true;
            }
        }
    }

    
    
    @Override
    public void onEvent(MovementResponse paramMovementResponse, String sourceEndpointKey) {
        if (checkEventAllowed(paramMovementResponse, sourceEndpointKey)) {
            LOG.info("Got movement response [{}] for request [{}] from [{}]", paramMovementResponse.getAccepted(),
                    paramMovementResponse.getRequestId(), sourceEndpointKey);
            updateResponses(paramMovementResponse);
        }
    }

    @Override
    public void onEvent(ExitFoundNotice paramExitFoundNotice, String sourceEndpointKey) {
        if (checkEventAllowed(paramExitFoundNotice, sourceEndpointKey)) {
            LOG.info("Exit found  by [{}]", sourceEndpointKey);
            boolean reportExitFoundCompleted = !robotPositions.isEmpty();
            robotPositions.remove(sourceEndpointKey);
            reportExitFoundCompleted &= robotPositions.isEmpty();
            LOG.debug("Removed robot due to ExitFoundNotice, robot count [{}].", getRobotCount());
            Cell cell = locationToCell(paramExitFoundNotice.getExitLocation());
            for (EventListener listener : listeners) {
                listener.onExitFound(cell);
            }
            if (reportExitFoundCompleted) {
                LOG.debug("All robots escaped!");
                for (EventListener listener : listeners) {
                    listener.onExitFoundCompeted();
                }
            }
        }
    }

    @Override
    public synchronized void requestMove(Cell cell, MoveEventListener moveListener) {
        LOG.debug("Got move request to [{}]", cell);
        targetCell = cell;
        targetCellApproved = false;
        final List<String> keys = new ArrayList<>(robotPositions.keySet());
        int robotCount = keys.size();
        if (robotCount > 0) {            
            moveRequestLatch = new CountDownLatch(robotCount);
            moveRequestId = UUID.randomUUID().toString();
            moveRequestResponses = new HashSet<Boolean>(robotCount);
            synchronized (conflictLock) {
                moveRequestConflict = false;
            }
            LOG.debug("Send move request {} to {} robots", moveRequestId, robotCount);
            eventExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    MovementRequest request = new MovementRequest(moveRequestId, cellToLocation(targetCell));
                    for (String key : keys) {
                        family.sendEvent(request, key);
                    }
                }
            });
            try {
                if (moveRequestLatch.await(PROCESSING_TIME_OUT, TimeUnit.MILLISECONDS)) {
                    boolean allowed = analyzeResponses();
                    if (allowed) {
                        targetCellApproved = true;
                        LOG.debug("Move allowed for request {}", moveRequestId);
                        moveAllowed(moveListener);
                    } else {
                        LOG.debug("Move forbidden for request {}", moveRequestId);
                        moveForbidden(moveListener, Reason.CONFLICT);
                    }
                } else {
                    LOG.debug("Received timeout while waiting responses for move request {}", moveRequestId);
                    moveForbidden(moveListener, Reason.TIMEOUT);
                }
                ;
            } catch (InterruptedException e) {
                LOG.debug("Received error while waiting responses for move request {}", moveRequestId);
                moveForbidden(moveListener, Reason.ERROR);
            }
        } else {
            LOG.debug("There is no other robots, so, move request is allowed!");
            moveAllowed(moveListener);
        }
    }
    
    private void moveAllowed(final MoveEventListener moveListener) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                moveListener.onMoveAllowed();
            }
        });
    }
    
    private void moveForbidden(final MoveEventListener moveListener, final Reason reason) {
        eventExecutor.submit(new Runnable() {
            @Override
            public void run() {
                moveListener.onMoveForbidden(reason);
            }
        });
    }

    private Location cellToLocation(Cell cell) {
        if (cell != null) {
            Location location = new Location();
            location.setX(cell.getX());
            location.setY(cell.getY());
            return location;
        } else {
            throw new RuntimeException("Cell can't be null.");
        }
    }

    private Cell locationToCell(Location location) {
        if (location != null) {
            return dataProvider.getLabyrinth().getCell(location.getX(), location.getY());
        } else {
            throw new RuntimeException("Location can't be null.");
        }
    }

    private EntityInfo buildEntityInfo() {
        return buildEntityInfo(null);
    }

    private EntityInfo buildEntityInfo(Cell cell) {
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.setEntityType(entityType);
        Location location = null;
        if (entityType == EntityType.ROBOT) {
            location = cellToLocation(cell);
            entityInfo.setDeadEnd(cell.isDeadEnd());
        } else {
            location = new Location();
            location.setX(-1);
            location.setY(-1);
            entityInfo.setDeadEnd(false);
        }
        entityInfo.setLocation(location);
        entityInfo.setName(dataProvider.getEntityName());
        return entityInfo;
    }

    private void updateResponses(MovementResponse paramMovementResponse) {
        if (paramMovementResponse.getRequestId().equals(moveRequestId)) {
            if (moveRequestResponses != null) {
                synchronized (moveRequestResponses) {
                    moveRequestResponses.add(paramMovementResponse.getAccepted());
                }
            } else {
                throw new IllegalStateException("Got response on uninitialized request!");
            }
            moveRequestLatch.countDown();
        }
    }

    private boolean analyzeResponses() {
        boolean allowed = true;
        synchronized (conflictLock) {
            if (moveRequestConflict) {
                return false;
            }
        }
        if (moveRequestResponses != null) {
            synchronized (moveRequestResponses) {
                for (Boolean response : moveRequestResponses) {
                    if (!response) {
                        allowed = false;
                        break;
                    }
                }
            }
        }
        return allowed;
    }

    private boolean checkEventAllowed(Object event, String sourceEndpointKey) {
        if (attachedEndpoints.contains(sourceEndpointKey)) {
            return true;
        } else {
            LOG.warn("Ignoring {} event from unknown source {}", event.getClass().getName(), sourceEndpointKey);
            return false;
        }
    }
    
    public interface EventManagerDataProvider {
        Cell getCurrentCell();
        Labyrinth getLabyrinth();
        String getEntityName();
    }
    
}
