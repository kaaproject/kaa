/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.thrift.cli.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.cli.CommandResult;
import org.kaaproject.kaa.server.common.thrift.gen.cli.CommandStatus;
import org.kaaproject.kaa.server.common.thrift.gen.cli.MemoryUsage;
import org.kaaproject.kaa.server.common.thrift.util.ThriftExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class BaseCliThriftService.<br>
 * Basic abstract class implementing default thrift CLI commands
 */
public abstract class BaseCliThriftService implements CliThriftService.Iface {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(BaseCliThriftService.class);

    /** The Constant MBYTE. */
    private static final long MBYTE = 1024 * 1024;

    /** The Thrift CLI commands map. */
    private Map<String, Command> commandsMap = new LinkedHashMap<>();

    /**
     * Instantiates a new base cli thrift service.
     */
    public BaseCliThriftService() {
        initDefaultCommands();
        initServiceCommands();
    }

    /**
     * Inits the default thrift CLI commands.
     */
    private void initDefaultCommands() {
        Command helpCommand = new Command("help", "display available commands") {
            @Override
            public void runCommand(CommandLine line, PrintWriter writer) {
                listCommands(writer);
            }
        };
        Command memoryCommand = new Command("memory",
                "display server memory info") {

            @Override
            public void runCommand(CommandLine line, PrintWriter writer) {
                printMemory(writer, line.hasOption('g'));
            }
        };

        memoryCommand.addOption(new Option("g", "gc", false,
                "Force Garbage Collector before memory status"));

        Command threadsCommand = new Command("threads", "dump JVM threads") {

            @Override
            public void runCommand(CommandLine line, PrintWriter writer) {
                dumpThreads(writer);
            }
        };

        Command shutdownCommand = new Command("shutdown", "shutdown server") {

            @Override
            public void runCommand(CommandLine line, PrintWriter writer) {
                shutdown(writer);
            }
        };

        addCommand(helpCommand);
        addCommand(memoryCommand);
        addCommand(threadsCommand);
        addCommand(shutdownCommand);
    }

    /**
     * Gets the thrift server short name used to display in thrift cli console.
     * 
     * @return the server short name
     */
    protected abstract String getServerShortName();

    /**
     * Inits the service specific CLI commands.
     */
    protected abstract void initServiceCommands();

    /**
     * Adds the CLI command and inits default command options.
     * 
     * @param command
     *            the command
     */
    protected void addCommand(Command command) {
        command.addOption(new Option("h", "help", false,
                "Print command help information"));
        commandsMap.put(command.getCommand(), command);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService
     * .Iface#serverName()
     */
    @Override
    public String serverName() throws TException {
        return getServerShortName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService
     * .Iface#shutdown()
     */
    @Override
    public void shutdown() throws TException {
        LOG.info("Received shutdown command.");
        Runnable shutdownCommand = new Runnable() {
            @Override
            public void run() {
                try {
                    ThriftExecutor.shutdown();
                    System.exit(0); //NOSONAR
                } catch (Exception e){
                    LOG.error("Catch exception when execute shutdown command", e);
                }
            }
        };
        new Thread(shutdownCommand).start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService
     * .Iface#getMemoryUsage(boolean)
     */
    @Override
    public MemoryUsage getMemoryUsage(boolean forceGC)
            throws TException {
        if (forceGC) {
            System.gc(); //NOSONAR
        }
        MemoryUsage memUsage = new MemoryUsage();
        memUsage.setMax(Runtime.getRuntime().maxMemory());
        memUsage.setFree(Runtime.getRuntime().freeMemory());
        memUsage.setTotal(Runtime.getRuntime().totalMemory());
        return memUsage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService
     * .Iface#executeCommand(java.lang.String)
     */
    @Override
    public CommandResult executeCommand(String commandLineString)
            throws TException {

        CommandStatus status = CommandStatus.OK;
        String[] args = commandLineString.split(" ");

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
        String commandString = args[0];
        Command command = commandsMap.get(commandString);
        if (command != null) {
            CommandLineParser cmdLinePosixParser = new PosixParser();
            String[] commandArgs = new String[args.length - 1];
            System.arraycopy(args, 1, commandArgs, 0, args.length - 1);
            try {
                CommandLine commandLine = cmdLinePosixParser.parse(
                        command.getOptions(), commandArgs);
                if (commandLine.hasOption('h')) {
                    printHelp(command, writer);
                } else {
                    command.runCommand(commandLine, writer);
                }
            } catch (ParseException e) {
                writer.println("Unable to parse command arguments.");
                writer.println();
                printHelp(command, writer);
                status = CommandStatus.ERROR;
            }
        } else {
            writer.println("Error: unknown command '" + commandString + "'");
            listCommands(writer);
        }

        CommandResult result = new CommandResult();
        writer.println();
        writer.flush();
        result.message = outStream.toString();
        result.status = status;

        return result;
    }

    /**
     * thrift cli console help command to display available commands.
     * 
     * @param writer
     *            the writer to write help output
     */
    private void listCommands(PrintWriter writer) {
        writer.println("Available commands:");
        writer.println();
        int max = 0;
        for (String command : commandsMap.keySet()) {
            max = Math.max(max, command.length());
        }
        for (String command : commandsMap.keySet()) {
            String desc = commandsMap.get(command).getDesc();
            StringBuffer commandBuf = new StringBuffer();
            commandBuf.append(command);
            if (commandBuf.length() < max) {
                commandBuf.append(createPadding(max - commandBuf.length()));
            }
            commandBuf.append("     ");
            commandBuf.append(desc);
            writer.println(commandBuf.toString());
        }
        writer.println();
        writer.println("To see command usage execute: <command> -h");
    }

    /**
     * Prints service memory usage.
     * 
     * @param writer
     *            the writer to output memory usage info
     * @param forceGC
     *            force Garbage Collection before obtaining memory information
     */
    private void printMemory(PrintWriter writer, boolean forceGC) {
        writer.println("Memory Usage:");
        writer.println();
        try {
            MemoryUsage memUsage = getMemoryUsage(forceGC);

            NumberFormat format = new DecimalFormat("0.#");

            writer.println("Max available  : "
                    + format.format((float) memUsage.max / (float) MBYTE)
                    + " MBytes");
            writer.println("Current heap   : "
                    + format.format((float) memUsage.total / (float) MBYTE)
                    + " MBytes");
            writer.println("Used           : "
                    + format.format((float) (memUsage.total - memUsage.free)
                    / (float) MBYTE) + " MBytes");
        } catch (TException e) {
            LOG.error("Catch exception when execute print memory command", e);
        }
    }

    /**
     * Dump service threads information.
     * 
     * @param writer
     *            the writer to output threads information
     */
    private void dumpThreads(PrintWriter writer) {
        writer.println("THREADS DUMP:");
        writer.println();

        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threads = threadMxBean.dumpAllThreads(false, false);
        Map<Long, ThreadStruct> threadsMap = new HashMap<>();
        for (ThreadInfo ti : threads) {
            ThreadStruct ts = new ThreadStruct();
            ts.ti = ti;
            threadsMap.put(ti.getThreadId(), ts);
        }

        ThreadGroup root = Thread.currentThread().getThreadGroup();

        ThreadGroup parent;
        parent = root.getParent();
        while (parent != null) {
            root = parent;
            parent = parent.getParent();
        }
        allThreadsFromGroup(root, threadsMap);

        Collection<ThreadStruct> threadValues = threadsMap.values();
        List<ThreadStruct> threadList = new ArrayList<>(
                threadValues);

        Collections.sort(threadList);

        Map<State, Integer> threadStatistics = new LinkedHashMap<>();
        threadStatistics.put(State.NEW, 0);
        threadStatistics.put(State.RUNNABLE, 0);
        threadStatistics.put(State.BLOCKED, 0);
        threadStatistics.put(State.WAITING, 0);
        threadStatistics.put(State.TIMED_WAITING, 0);
        threadStatistics.put(State.TERMINATED, 0);
        int maxGroup = 0;
        int maxName = 0;
        for (ThreadStruct thread : threadList) {
            maxName = Math.max(thread.ti.getThreadName().length(), maxName);
            maxGroup = Math.max(thread.getGroupName().length(), maxGroup);
            int count = threadStatistics.get(thread.ti.getThreadState());
            count++;
            threadStatistics.put(thread.ti.getThreadState(), count);
        }

        int idColumnLength = 4;
        int groupColumnLength = maxGroup + 1;
        int nameColumnLength = maxName + 1;
        int priorityColumnLength = 10;
        int stateColumnLength = 14;
        int daemonColumnLengh = 7;
        int aliveColumnLengh = 6;
        int cpuTimeColumnLengh = 14;

        StringBuffer header = new StringBuffer();
        header.append("ID");
        int length = idColumnLength;
        header.append(createPadding(length - header.length()));
        header.append("GROUP");
        length += groupColumnLength;
        header.append(createPadding(length - header.length()));
        header.append("NAME");
        length += nameColumnLength;
        header.append(createPadding(length - header.length()));
        header.append("PRIORITY");
        length += priorityColumnLength;
        header.append(createPadding(length - header.length()));
        header.append("STATE");
        length += stateColumnLength;
        header.append(createPadding(length - header.length()));
        header.append("DAEMON");
        length += daemonColumnLengh;
        header.append(createPadding(length - header.length()));
        header.append("ALIVE");
        length += aliveColumnLengh;
        header.append(createPadding(length - header.length()));
        header.append("CPU TIME (SEC)");
        length += cpuTimeColumnLengh;
        header.append(createPadding(length - header.length()));
        writer.println(header);

        int maxRowLength = header.length();

        writer.println(createPadding(maxRowLength, '-'));

        NumberFormat format = new DecimalFormat("0.#");

        for (ThreadStruct thread : threadList) {
            StringBuffer row = new StringBuffer();
            row.append(thread.ti.getThreadId());
            int rowLength = idColumnLength;
            row.append(createPadding(rowLength - row.length()));
            row.append(thread.getGroupName());
            rowLength += groupColumnLength;
            row.append(createPadding(rowLength - row.length()));
            row.append(thread.ti.getThreadName());
            rowLength += nameColumnLength;
            row.append(createPadding(rowLength - row.length()));
            row.append(thread.getPriority());
            rowLength += priorityColumnLength;
            row.append(createPadding(rowLength - row.length()));
            row.append(thread.ti.getThreadState());
            rowLength += stateColumnLength;
            row.append(createPadding(rowLength - row.length()));
            row.append(thread.isDaemon());
            rowLength += daemonColumnLengh;
            row.append(createPadding(rowLength - row.length()));
            row.append(thread.isAlive());
            rowLength += aliveColumnLengh;
            row.append(createPadding(rowLength - row.length()));
            double cpuTimeSec = (double) threadMxBean
                    .getThreadCpuTime(thread.ti.getThreadId())
                    / (double) (1000 * 1000 * 1000);
            row.append(format.format(cpuTimeSec));
            writer.println(row);
        }

        writer.println(createPadding(maxRowLength, '-'));
        writer.println("SUMMARY:");
        writer.println(createPadding(maxRowLength, '-'));

        for (State state : threadStatistics.keySet()) {
            int count = threadStatistics.get(state);
            if (count > 0) {
                StringBuffer row = new StringBuffer();
                row.append(state.toString());
                row.append(createPadding(stateColumnLength - row.length()));
                row.append(count);
                writer.println(row);
            }
        }
        writer.println(createPadding(maxRowLength, '-'));
        StringBuffer row = new StringBuffer();
        row.append("TOTAL");
        row.append(createPadding(stateColumnLength - row.length()));
        row.append(threadList.size());
        writer.println(row);
        writer.println(createPadding(maxRowLength, '-'));
    }

    /**
     * Retrieve all threads info from thread group.
     * 
     * @param group
     *            the thread group
     * @param threadsMap
     *            the threads map to store threads info
     */
    private void allThreadsFromGroup(ThreadGroup group,
            Map<Long, ThreadStruct> threadsMap) {
        int tCount = group.activeCount();
        int gCount = group.activeGroupCount();
        Thread[] gThreads = new Thread[tCount];
        ThreadGroup[] tGroups = new ThreadGroup[gCount];
        group.enumerate(gThreads, false);
        group.enumerate(tGroups, false);
        for (Thread t : gThreads) {
            if (t != null) {
                ThreadStruct ts = threadsMap.get(t.getId());
                ts.t = t;
            }
        }
        for (ThreadGroup tg : tGroups) {
            allThreadsFromGroup(tg, threadsMap);
        }
    }

    /**
     * The Class ThreadStruct. Used to store thread information. Implements
     * comparison mechanism to sort threads information.
     */
    class ThreadStruct implements Comparable<ThreadStruct> {

        /** The thread. */
        public Thread t;

        /** The thread info. */
        public ThreadInfo ti;

        /**
         * Gets the thread group name.
         *
         * @return the group name
         */
        public String getGroupName() {
            return t != null ? t.getThreadGroup().getName() : "";
        }

        /**
         * Gets the thread priority.
         *
         * @return the priority
         */
        public String getPriority() {
            return t != null ? t.getPriority() + "" : "";
        }

        /**
         * Checks if thread is daemon.
         *
         * @return the "daemon" string if thread is daemon otherwise empty
         *         string
         */
        public String isDaemon() {
            return t != null && t.isDaemon() ? "daemon" : "";
        }

        /**
         * Checks if thread is alive.
         *
         * @return the "alive" string if thread is alive otherwise empty string
         */
        public String isAlive() {
            return t != null && t.isAlive() ? "alive" : "";
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(ThreadStruct o) {
            int result = getGroupName().compareTo(o.getGroupName());
            if (result == 0) {
                result = (int) (ti.getThreadId() - o.ti.getThreadId());
            }
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ThreadStruct)) {
                return false;
            }

            ThreadStruct that = (ThreadStruct) o;

            if (t != null ? !t.equals(that.t) : that.t != null) {
                return false;
            }
            if (ti != null ? !ti.equals(that.ti) : that.ti != null) {
                return false;
            }

            return true;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode(java.lang.Object)
         */
        @Override
        public int hashCode() {
            int result = t != null ? t.hashCode() : 0;
            result = 31 * result + (ti != null ? ti.hashCode() : 0);
            return result;
        }
    }

    /**
     * Shutdown server.
     * 
     * @param writer
     *            the writer to output shutdown information.
     */
    private void shutdown(PrintWriter writer) {
        writer.println("Server shutdown initiated.");
        try {
            shutdown();
        } catch (TException e) {
            LOG.error("Catch exception when execute shutdown command", e);
        }
    }

    /**
     * Creates the string padding.
     * 
     * @param len
     *            length of the padding
     * @return the resulting padding string
     */
    protected String createPadding(int len) {
        StringBuffer sb = new StringBuffer(len);

        for (int i = 0; i < len; ++i) {
            sb.append(' ');
        }

        return sb.toString();
    }

    /**
     * Creates the padding using specified character.
     * 
     * @param len
     *            length of the padding
     * @param c
     *            the character to use for padding
     * @return the resulting padding string
     */
    protected String createPadding(int len, char c) {
        StringBuffer sb = new StringBuffer(len);

        for (int i = 0; i < len; ++i) {
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Prints the CLI command usage.
     * 
     * @param command
     *            the CLI command
     * @param writer
     *            the writer to write usage output
     */
    private void printHelp(Command command, PrintWriter writer) {
        writer.println(command.getCommand() + " - " + command.getDesc());
        writer.println();
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 80, command.getCommand(), "Options",
                command.getOptions(), 3, 5, "", true);
    }

}
