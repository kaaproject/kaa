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

package org.kaaproject.kaa.server.control.cli;

import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDataStruct;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.core.schema.KaaSchemaFactoryImpl;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.kaaproject.kaa.server.common.thrift.gen.control.SdkPlatform;
import org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ControlApiCommandProcessor.<br>
 * Used to process Control API commands from API console.
 */
public class ControlApiCommandProcessor {
    /* The constant logger */ 
    private static final Logger LOG = LoggerFactory.getLogger(ControlApiCommandProcessor.class);

    /** The Constant UTF8. */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String CREATE = "create";
    private static final String TENANT_ID = "tenantId";
    private static final String TENANT_ID_OPTION = "Tenant Id option";
    private static final String OUTPUT = "output";
    private static final String OUTPUT_FILE_TO_STORE_OBJECT_ID = "Output file to store Object Id";
    private static final String NOT_FOUND = " not found!";
    private static final String UNABLE_TO = "Unable to ";
    private static final String UPDATE = "update";
    private static final String APPLICATION_ID = "applicationId";
    private static final String APPLICATION_ID_OPTION = "Application Id option";
    private static final String PROFILE_SCHEMA = " Profile Schema";
    private static final String PROFILE_SCHEMA_ID = "profileSchemaId";
    private static final String PROFILE_SCHEMA_ID_OPTION = "Profile Schema Id option";
    private static final String VERSION_OUTPUT = "versionOutput";
    private static final String OUTPUT_FILE_TO_STORE_VERSION = "Output file to store version";
    private static final String CONFIGURATION_SCHEMA = " Configuration Schema";
    private static final String CONFIGURATION_SCHEMA_ID = "configurationSchemaId";
    private static final String CONFIGURATION_SCHEMA_ID_OPTION = "Configuration Schema Id option";
    private static final String LOG_SCHEMA = " Log Schema";
    private static final String LOG_SCHEMA_ID = "logSchemaId";
    private static final String LOG_SCHEMA_ID_OPTION = "Log Schema Id option";
    private static final String ENDPOINT_GROUP = " Endpoint Group";
    private static final String ENDPOINT_GROUP_ID = "endpointGroupId";
    private static final String ENDPOINT_GROUP_ID_OPTION = "Endpoint Group Id option";
    private static final String TOPIC_ID = "topicId";
    private static final String TOPIC_ID_OPTION = "Topic Id option";
    private static final String INCORRECT_ENDPOINT_GROUP_ID = "Incorrect endpoint group id.";
    private static final String INCORRECT_TOPIC_ID = "Incorrect topic id.";
    private static final String PROFILE_FILTER = " Profile Filter";
    private static final String PROFILE_FILTER_ID = "profileFilterId";
    private static final String PROFILE_FILTER_ID_OPTION = "Profile Filter Id option";
    private static final String UNABLE_TO_ACTIVATE_CONFIGURATION = "Unable to activate Configuration";
    private static final String CONFIGURATION = " Configuration";
    private static final String CONFIGURATION_ID = "configurationId";
    private static final String CONFIGURATION_ID_OPTION = "Configuration Id option";
    private static final String TOPIC = " Topic";
    private static final String NOTIFICATION = " Notification";
    private static final String SCHEMA_ID = "schema-id";
    private static final String NOTIFICATION_SCHEMA_ID_OPTION = "Notification schema id option";
    private static final String NOTIFICATION_TOPIC_ID_OPTION = "Notification topic id option.";
    private static final String TOPIC__ID = "topic-id"; //NOSONAR
    private static final String NOTIFICATION_BODY_FILE_OPTION = "Notification body file option.";
    private static final String BODY_FILE = "body-file";
    private static final String NOTIFICATION_BODY_OPTION = "Notification body option.";
    private static final String INVALID_SCHEMA_ID_FOR_NOTIFICATION = "Invalid schema id for notification.";
    private static final String INVALID_TOPIC_ID_FOR_NOTIFICATION = "Invalid topic id for notification.";
    private static final String INCORRECT_FORMAT_OF_TTL = "Incorrect format of ttl: ";
    private static final String CANT_READ_FILE = "Can't read file. Please check file name.";
    private static final String NEED_TO_SET_BODY = "Need to set body or file with body for notification";
    private static final String ID_OPTION = " Id option";
    private static final String OUTPUT_FILE_TO_STORE_IDS = "Output file to store Object Ids";
    private static final String TOTAL = "Total: ";
    private static final String SPECIFIED_FILE = "Specified file '";

    /**
     * The Enum EntityType. Main types of processed entities.
     */
    public enum EntityType {

        /** The tenant. */
        TENANT("Tenant"),

        /** The user. */
        USER("User"),

        /** The application. */
        APPLICATION("Application"),

        /** The profile schema. */
        PROFILE_SCHEMA("ProfileSchema"),

        /** The configuration schema. */
        CONFIGURATION_SCHEMA("ConfigurationSchema"),
        
        /** The log schema. */
        LOG_SCHEMA("LogSchema"),

        /** The endpoint group. */
        ENDPOINT_GROUP("EndpointGroup"),

        /** The profile filter. */
        PROFILE_FILTER("ProfileFilter"),

        /** The configuration. */
        CONFIGURATION("Configuration"),

        /** The topic. */
        TOPIC("Topic"),

        /** The notification. */
        NOTIFICATION("Notification"),

        /** The notification. */
        NOTIFICATION_SCHEMA("NotificationSchema"),

        /** The unicast notification. */
        PERSONAL_NOTIFICATION("UnicastNotification"),

        /** The endpoint user. */
        ENDPOINT_USER("EndpointUser");

        /** The name. */
        String name;

        /**
         * Instantiates a new entity type.
         *
         * @param name
         *            the name
         */
        EntityType(String name) {
            this.name = name;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        String getName() {
            return name;
        }
    }

    /** The control API commands map. */
    private final Map<String, ControlApiCommand> commandsMap = new LinkedHashMap<String, ControlApiCommand>();

    /**
     * Instantiates a new control api command processor.
     */
    public ControlApiCommandProcessor() {
        initApiCommands();
    }

    /**
     * Inits the api commands.
     */
    private void initApiCommands() {
        addCommand(helpCommand());
        addCommand(createTenantCommand(false));
        addCommand(createTenantCommand(true));
        addCommand(createUserCommand(false));
        addCommand(createUserCommand(true));
        addCommand(createApplicationCommand(false));
        addCommand(createApplicationCommand(true));
        addCommand(createProfileSchemaCommand(false));
        addCommand(createProfileSchemaCommand(true));
        addCommand(createConfigurationSchemaCommand(false));
        addCommand(createConfigurationSchemaCommand(true));
        addCommand(createLogSchemaCommand(false));
        addCommand(createLogSchemaCommand(true));
        addCommand(createEndpointGroupCommand(false));
        addCommand(createEndpointGroupCommand(true));
        addCommand(createProfileFilterCommand(false));
        addCommand(createProfileFilterCommand(true));
        addCommand(activateProfileFilterCommand());
        addCommand(createConfigurationCommand(false));
        addCommand(createConfigurationCommand(true));
        addCommand(activateConfigurationCommand());

        addCommand(removeTopicFromEndpointGroupCommand());
        addCommand(addTopicToEndpointGroupCommand());
        addCommand(createTopicCommand(true));
        addCommand(createTopicCommand(false));

        addCommand(createNotificationCommand());

        addCommand(createNotificationSchemaCommand(true));
        addCommand(createNotificationSchemaCommand(false));

        addCommand(createUnicastNotificationCommand());

        addCommand(createEndpointUserCommand(false));
        addCommand(createEndpointUserCommand(true));

        addCommand(listCommand(EntityType.USER));
        addCommand(listCommand(EntityType.TENANT));
        addCommand(listCommand(EntityType.TOPIC));
        addCommand(listCommand(EntityType.ENDPOINT_USER));

        addCommand(listApplicationsCommand());
        addCommand(listProfileSchemasCommand());
        addCommand(listConfigurationSchemasCommand());

        addCommand(listNotificationsCommand());
        addCommand(listNotificationSchemasCommand());
        addCommand(listTopicsCommand());

        addCommand(listEndpointGroupsCommand());

        for (EntityType type : EntityType.values()) {
            addCommand(showCommand(type));
            addCommand(deleteCommand(type));
        }

        addCommand(generateSdkCommand());

    }

    /**
     * Adds the API command and inits default command options.
     *
     * @param command
     *            the control api command
     */
    private void addCommand(ControlApiCommand command) {
        command.addOption(new Option("h", "help", false,
                "Print command help information"));
        commandsMap.put(command.getCommand(), command);
    }

    /**
     * Execute control API command.
     *
     * @param ss
     *            the Control Clisent Session state
     * @param commandLineString
     *            the command line string
     */
    public void executeCommand(ControlClientSessionState ss,
            String commandLineString) {
        PrintWriter writer = new PrintWriter(ss.out);
        PrintWriter errorWriter = new PrintWriter(ss.err);
        String[] args = null;
        try {
            args = parseCommand(commandLineString);
        } catch (ParseException e) {
            writer.println("Unable to parse command line: "
                    + e.getMessage());
            writer.println();
        }
        if (args != null && args.length>0) {
            String commandString = args[0];
            ControlApiCommand command = commandsMap.get(commandString);
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
                        command.runCommand(commandLine, ss.getClient(), writer,
                                errorWriter);
                    }
                } catch (ParseException e) {
                    writer.println("Unable to parse command arguments: "
                            + e.getMessage());
                    writer.println();
                    printHelp(command, writer);
                }
            } else {
                writer.println("Error: unknown command '" + commandString + "'");
                help(writer);
            }
        }
        writer.println();
        writer.flush();
        errorWriter.flush();
    }

    /**
     * Parses the command.
     *
     * @param toProcess the to process
     * @return the string[]
     * @throws ParseException the parse exception
     */
    private static String[] parseCommand(String toProcess) throws ParseException {
        if (toProcess == null || toProcess.length() == 0) {
            //no command? no string
            return new String[0];
        }
        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        Vector<String> v = new Vector<>();
        StringBuffer current = new StringBuffer();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
            case inQuote:
                if ("\'".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            case inDoubleQuote:
                if ("\"".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            default:
                if ("\'".equals(nextTok)) {
                    state = inQuote;
                } else if ("\"".equals(nextTok)) {
                    state = inDoubleQuote;
                } else if (" ".equals(nextTok)) {
                    if (lastTokenHasBeenQuoted || current.length() != 0) {
                        v.addElement(current.toString());
                        current = new StringBuffer();
                    }
                } else {
                    current.append(nextTok);
                }
                lastTokenHasBeenQuoted = false;
                break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            v.addElement(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new ParseException("unbalanced quotes in " + toProcess);
        }
        String[] args = new String[v.size()];
        v.copyInto(args);
        return args;
    }

    /**
     * Creates the string padding.
     *
     * @param len
     *            length of the padding
     * @return the resulting padding string
     */
    private String createPadding(int len) {
        StringBuffer sb = new StringBuffer(len);

        for (int i = 0; i < len; ++i) {
            sb.append(' ');
        }

        return sb.toString();
    }

    /**
     * Prints the control api command usage.
     *
     * @param command
     *            the control api command
     * @param writer
     *            the writer to write usage output
     */
    private void printHelp(ControlApiCommand command, PrintWriter writer) {
        writer.println(command.getCommand() + " - " + command.getDesc());
        writer.println();
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 80, command.getCommand(), "Options",
                command.getOptions(), 3, 5, "", true);
    }

    /**
     * control api help command to display available commands.
     *
     * @return the control api help command
     */
    private ControlApiCommand helpCommand() {
        return new ControlApiCommand("help",
                "display available commands") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                help(writer);
            }
        };
    }

    /**
     * Prints Control API Help.
     *
     * @param writer
     *            the writer to write help output
     */
    private void help(PrintWriter writer) {
        writer.println("Available Control Server API commands:");
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
     * Creates the API command to work with tenant entities.
     *
     * @param edit
     *            edit else create tenant
     * @return the control api command
     */
    private ControlApiCommand createTenantCommand(final boolean edit) {
        ControlApiCommand command = new ControlApiCommand(edit ? "editTenant"
                : "createTenant", (edit ? "edit" : CREATE) + " Tenant") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                createTenant(line, client, writer, errorWriter, edit);
            }
        };
        if (edit) {
            Option opt = new Option("i", TENANT_ID, true,
                    TENANT_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
        }
        Option opt = new Option("n", "name", true, "Tenant Name option");
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the tenant.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     * @param edit
     *            edit else create tenant
     */
    private void createTenant(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {
        try {
            TenantDto tenant = null;
            if (edit) {
                String tenantId = line.getOptionValue("i");
                tenant = toDto(client.getTenant(tenantId));
                if (tenant == null) {
                    writer.println("Tenant with id " + tenantId
                            + NOT_FOUND);
                    return;
                }
            } else {
                tenant = new TenantDto();
            }
            if (line.hasOption("n")) {
                tenant.setName(line.getOptionValue("n"));
            }
            TenantDto savedTenant = toDto(client
                    .editTenant(toDataStruct(tenant)));
            if (edit) {
                writer.println("Tenant updated.");
            } else {
                writer.println("Created new tenant with id: "
                        + savedTenant.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedTenant.getId(),
                            errorWriter);
                }
            }
        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + " tenant", e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with user entities.
     *
     * @param edit
     *            edit else create user
     * @return the control api command
     */
    private ControlApiCommand createUserCommand(final boolean edit) {
        ControlApiCommand command = new ControlApiCommand(edit ? "editUser"
                : "createUser", (edit ? "edit" : CREATE) + " User") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                createUser(line, client, writer, errorWriter, edit);
            }
        };
        if (edit) {
            Option opt = new Option("i", "userId", true, "User Id option");
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("uid", "externalUid", true, "External user id option");
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("t", TENANT_ID, true, TENANT_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the user.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     * @param edit
     *            edit else create user
     */
    private void createUser(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {
        try {

            UserDto user = null;
            if (edit) {
                String userId = line.getOptionValue("i");
                user = toDto(client.getUser(userId));
                if (user == null) {
                    writer.println("User with id " + userId + NOT_FOUND);
                    return;
                }
            } else {
                user = new UserDto();
            }

            if (line.hasOption("uid")) {
                user.setExternalUid(line.getOptionValue("uid"));
            }
            if (line.hasOption("t")) {
                user.setTenantId(line.getOptionValue("t"));
            }

            UserDto savedUser = toDto(client.editUser(toDataStruct(user)));

            if (edit) {
                writer.println("User updated.");
            } else {
                writer.println("Created new user with id: " + savedUser.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedUser.getId(), errorWriter);
                }

            }
        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + " user", e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with application entities.
     *
     * @param edit
     *            edit else create application
     * @return the control api command
     */
    private ControlApiCommand createApplicationCommand(final boolean edit) {

        ControlApiCommand command = new ControlApiCommand(
                edit ? "editApplication" : "createApplication", (edit ? "edit"
                        : CREATE) + " Application") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                createApplication(line, client, writer, errorWriter, edit);
            }
        };
        if (edit) {
            Option opt = new Option("i", APPLICATION_ID, true,
                    APPLICATION_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("n", "name", true, "Application Name option");
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("t", TENANT_ID, true, TENANT_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the application.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     * @param edit
     *            edit else create application
     */
    private void createApplication(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {
        try {

            ApplicationDto application = null;
            if (edit) {
                String applicationId = line.getOptionValue("i");
                application = toDto(client.getApplication(applicationId));
                if (application == null) {
                    writer.println("Application with id " + applicationId
                            + NOT_FOUND);
                    return;
                }
            } else {
                application = new ApplicationDto();
            }

            if (line.hasOption("n")) {
                application.setName(line.getOptionValue("n"));
            }
            if (line.hasOption("t")) {
                application.setTenantId(line.getOptionValue("t"));
            }

            ApplicationDto savedApplication = toDto(client
                    .editApplication(toDataStruct(application)));

            if (edit) {
                writer.println("Application updated.");
            } else {
                writer.println("Created new application with id: "
                        + savedApplication.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedApplication.getId(),
                            errorWriter);
                }

            }

        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + " application", e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with profile schema entities.
     *
     * @param edit
     *            edit else create profile schema
     * @return the control api command
     */
    private ControlApiCommand createProfileSchemaCommand(final boolean edit) {
        ControlApiCommand command = new ControlApiCommand(
                edit ? "editProfileSchema" : "createProfileSchema",
                (edit ? "edit" : CREATE) + PROFILE_SCHEMA) {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                createProfileSchema(line, client, writer, errorWriter, edit);
            }
        };
        if (edit) {
            Option opt = new Option("i", PROFILE_SCHEMA_ID, true,
                    PROFILE_SCHEMA_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
            opt = new Option("vo", VERSION_OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_VERSION);
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("f", "file", true, "Profile Schema JSON File");
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("a", APPLICATION_ID, true, APPLICATION_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the profile schema.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     * @param edit
     *            edit else create profile schema
     */
    private void createProfileSchema(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {

        try {

            ProfileSchemaDto profileSchema = null;
            if (edit) {
                String profileSchemaId = line.getOptionValue("i");
                profileSchema = toDto(client.getProfileSchema(profileSchemaId));
                if (profileSchema == null) {
                    writer.println("Profile Schema with id " + profileSchemaId
                            + NOT_FOUND);
                    return;
                }
            } else {
                profileSchema = new ProfileSchemaDto();
            }

            if (line.hasOption("f")) {
                String schemaFile = line.getOptionValue("f");
                String schema = readFile(schemaFile, errorWriter);
                if (schema != null) {
                    profileSchema.setSchema(new KaaSchemaFactoryImpl().createDataSchema(schema).getRawSchema());
                } else {
                    return;
                }
            }

            if (line.hasOption("a")) {
                profileSchema.setApplicationId(line.getOptionValue("a"));
            }

            ProfileSchemaDto savedProfileSchema = toDto(client
                    .editProfileSchema(toDataStruct(profileSchema)));

            if (edit) {
                writer.println("Profile Schema updated.");
            } else {
                writer.println("Created new Profile Schema with id: "
                        + savedProfileSchema.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedProfileSchema.getId(),
                            errorWriter);
                }
                if (line.hasOption("vo")) {
                    String outFileName = line.getOptionValue("vo");
                    storeInfo(outFileName, ""+savedProfileSchema.getMajorVersion(),
                            errorWriter);
                }

            }

        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + PROFILE_SCHEMA, e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with configuration schema entities.
     *
     * @param edit
     *            edit else create configuration schema
     * @return the control api command
     */
    private ControlApiCommand createConfigurationSchemaCommand(
            final boolean edit) {
        ControlApiCommand command = new ControlApiCommand(
                edit ? "editConfigurationSchema" : "createConfigurationSchema",
                (edit ? "edit" : CREATE) + CONFIGURATION_SCHEMA) {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                createConfigurationSchema(line, client, writer, errorWriter,
                        edit);
            }
        };
        if (edit) {
            Option opt = new Option("i", CONFIGURATION_SCHEMA_ID, true,
                    CONFIGURATION_SCHEMA_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
            opt = new Option("vo", VERSION_OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_VERSION);
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("f", "file", true,
                "Configuration Schema JSON File");
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("a", APPLICATION_ID, true, APPLICATION_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the configuration schema.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     * @param edit
     *            edit else create configuration schema
     */
    private void createConfigurationSchema(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {
        try {

            ConfigurationSchemaDto configurationSchema = null;
            if (edit) {
                String configurationSchemaId = line.getOptionValue("i");
                configurationSchema = toDto(client
                        .getConfigurationSchema(configurationSchemaId));
                if (configurationSchema == null) {
                    writer.println("Configuration Schema with id "
                            + configurationSchemaId + NOT_FOUND);
                    return;
                }
            } else {
                configurationSchema = new ConfigurationSchemaDto();
            }

            if (line.hasOption("f")) {
                String schemaFile = line.getOptionValue("f");
                String schema = readFile(schemaFile, errorWriter);
                if (schema != null) {
                    configurationSchema.setSchema(new KaaSchemaFactoryImpl().createDataSchema(schema).getRawSchema());
                } else {
                    return;
                }
            }

            if (line.hasOption("a")) {
                configurationSchema.setApplicationId(line.getOptionValue("a"));
            }

            ConfigurationSchemaDto savedConfigurationSchema = toDto(client
                    .editConfigurationSchema(toDataStruct(configurationSchema)));

            if (edit) {
                writer.println("Configuration Schema updated.");
            } else {
                writer.println("Created new Configuration Schema with id: "
                        + savedConfigurationSchema.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName,
                            savedConfigurationSchema.getId(), errorWriter);
                }
                if (line.hasOption("vo")) {
                    String outFileName = line.getOptionValue("vo");
                    storeInfo(outFileName,
                            ""+savedConfigurationSchema.getMajorVersion(), errorWriter);
                }
            }

        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + CONFIGURATION_SCHEMA, e, errorWriter);
        }
    }
    
    /**
     * Creates the API command to work with log schema entities.
     *
     * @param edit
     *            edit else create log schema
     * @return the control api command
     */
    private ControlApiCommand createLogSchemaCommand(final boolean edit) {
        ControlApiCommand command = new ControlApiCommand(
                edit ? "editLogSchema" : "createLogSchema",
                (edit ? "edit" : CREATE) + LOG_SCHEMA) {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                createLogSchema(line, client, writer, errorWriter, edit);
            }
        };
        if (edit) {
            Option opt = new Option("i", LOG_SCHEMA_ID, true,
                    LOG_SCHEMA_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
            opt = new Option("vo", VERSION_OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_VERSION);
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("f", "file", true, "Log Schema JSON File");
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("a", APPLICATION_ID, true, APPLICATION_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the log schema.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     * @param edit
     *            edit else create log schema
     */
    private void createLogSchema(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {

        try {

            LogSchemaDto logSchema = null;
            if (edit) {
                String logSchemaId = line.getOptionValue("i");
                logSchema = toDto(client.getLogSchema(logSchemaId));
                if (logSchema == null) {
                    writer.println("Log Schema with id " + logSchemaId
                            + NOT_FOUND);
                    return;
                }
            } else {
                logSchema = new LogSchemaDto();
            }

            if (line.hasOption("f")) {
                String schemaFile = line.getOptionValue("f");
                String schema = readFile(schemaFile, errorWriter);
                if (schema != null) {
                    logSchema.setSchema(new KaaSchemaFactoryImpl().createDataSchema(schema).getRawSchema());
                } else {
                    return;
                }
            }

            if (line.hasOption("a")) {
                logSchema.setApplicationId(line.getOptionValue("a"));
            }

            LogSchemaDto savedLogSchema = toDto(client
                    .editLogSchema(toDataStruct(logSchema)));

            if (edit) {
                writer.println("Log Schema updated.");
            } else {
                writer.println("Created new Log Schema with id: "
                        + savedLogSchema.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedLogSchema.getId(),
                            errorWriter);
                }
                if (line.hasOption("vo")) {
                    String outFileName = line.getOptionValue("vo");
                    storeInfo(outFileName, ""+savedLogSchema.getMajorVersion(),
                            errorWriter);
                }

            }

        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + LOG_SCHEMA, e, errorWriter);
        }
    }    

    /**
     * Creates the API command to work with endpoint group entities.
     *
     * @param edit
     *            edit else create endpoint group
     * @return the control api command
     */
    private ControlApiCommand createEndpointGroupCommand(final boolean edit) {

        ControlApiCommand command = new ControlApiCommand(
                edit ? "editEndpointGroup" : "createEndpointGroup",
                (edit ? "edit" : CREATE) + ENDPOINT_GROUP) {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                createEndpoingGroup(line, client, writer, errorWriter, edit);
            }
        };
        if (edit) {
            Option opt = new Option("i", ENDPOINT_GROUP_ID, true,
                    ENDPOINT_GROUP_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("n", "name", true, "Endpoint Group Name option");
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("a", APPLICATION_ID, true, APPLICATION_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("w", "weight", true, "Endpoint Group Weight option");
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the endpoint group.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     * @param edit
     *            edit else create endpoint group
     */
    private void createEndpoingGroup(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {
        try {

            EndpointGroupDto endpointGroup = null;
            if (edit) {
                String endpointGroupId = line.getOptionValue("i");
                endpointGroup = toDto(client.getEndpointGroup(endpointGroupId));
                if (endpointGroup == null) {
                    writer.println("Endpoint Group with id " + endpointGroupId
                            + NOT_FOUND);
                    return;
                }
            } else {
                endpointGroup = new EndpointGroupDto();
            }

            if (line.hasOption("n")) {
                endpointGroup.setName(line.getOptionValue("n"));
            }
            if (line.hasOption("a")) {
                endpointGroup.setApplicationId(line.getOptionValue("a"));
            }
            if (line.hasOption("w")) {
                String weightStr = line.getOptionValue("w");
                Integer weight;
                try { //NOSONAR
                    weight = Integer.valueOf(weightStr);
                } catch (NumberFormatException nfe) {
                    errorWriter.println("Unable to parse weight option!");
                    return;
                }
                endpointGroup.setWeight(weight);
            }
            EndpointGroupDto savedEndpointGroup = toDto(client
                    .editEndpointGroup(toDataStruct(endpointGroup)));

            if (edit) {
                writer.println("Endpoint Group updated.");
            } else {
                writer.println("Created new Endpoint Group with id: "
                        + savedEndpointGroup.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedEndpointGroup.getId(),
                            errorWriter);
                }

            }

        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + ENDPOINT_GROUP, e, errorWriter);
        }
    }

    /**
     * Creates API command to add topic to endpoint group.
     *
     * @return the control api command
     */
    private ControlApiCommand addTopicToEndpointGroupCommand() {

        ControlApiCommand command = new ControlApiCommand(
                "addTopicToEndpointGroup", "add Topic to Endpoint Group") {
            @Override
            public void runCommand(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {
                addTopicToEndpointGroup(line, client, writer, errorWriter);
            }
        };

        Option opt = new Option("i", ENDPOINT_GROUP_ID, true,
                ENDPOINT_GROUP_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);
        opt = new Option("t", TOPIC_ID, true,
                TOPIC_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);
        return command;
    }

    /**
     * Add topic to endpoint group.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void addTopicToEndpointGroup(CommandLine line,
                                       ControlThriftService.Iface client, PrintWriter writer,
                                       PrintWriter errorWriter) {
        try {
            String endpointGroupId = null;
            String topicId = null;
            if (line.hasOption("i")) {
                endpointGroupId = line.getOptionValue("i");
                if(!StringUtils.isNotBlank(endpointGroupId)) {
                    errorWriter.println(INCORRECT_ENDPOINT_GROUP_ID);
                    return;
                }
            }
            if(line.hasOption("t")) {
                topicId = line.getOptionValue("t");
                if(!StringUtils.isNotBlank(topicId)) {
                    errorWriter.println(INCORRECT_TOPIC_ID);
                    return;
                }
            }
            @SuppressWarnings("unused")
            EndpointGroupDto endpointGroupDto = toDto(client.addTopicsToEndpointGroup(endpointGroupId, topicId)); //NOSONAR
            writer.println("Topic was added to Endpoint Group.");
        } catch (TException e) {
            handleException("Unable add  topic to Endpoint Group ", e, errorWriter);
        }
    }

    /**
     * Creates API command to remove topic to endpoint group.
     *
     * @return the control api command
     */
    private ControlApiCommand removeTopicFromEndpointGroupCommand() {

        ControlApiCommand command = new ControlApiCommand(
                "removeTopicFromEndpointGroup", "remove Topic from Endpoint Group") {
            @Override
            public void runCommand(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {
                removeTopicFromEndpointGroup(line, client, writer, errorWriter);
            }
        };

        Option opt = new Option("i", ENDPOINT_GROUP_ID, true,
                ENDPOINT_GROUP_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);
        opt = new Option("t", TOPIC_ID, true,
                TOPIC_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);
        return command;
    }

    /**
     * Add topic to endpoint group.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void removeTopicFromEndpointGroup(CommandLine line,
                                         ControlThriftService.Iface client, PrintWriter writer,
                                         PrintWriter errorWriter) {
        try {
            String endpointGroupId = null;
            String topicId = null;
            if (line.hasOption("i")) {
                endpointGroupId = line.getOptionValue("i");
                if(!StringUtils.isNotBlank(endpointGroupId)) {
                    errorWriter.println(INCORRECT_ENDPOINT_GROUP_ID);
                    return;
                }
            }
            if(line.hasOption("t")) {
                topicId = line.getOptionValue("t");
                if(!StringUtils.isNotBlank(topicId)) {
                    errorWriter.println(INCORRECT_TOPIC_ID);
                    return;
                }
            }
            @SuppressWarnings("unused")
            EndpointGroupDto endpointGroupDto = toDto(client.removeTopicsFromEndpointGroup(endpointGroupId, topicId)); //NOSONAR
            writer.println("Topic was removed from Endpoint Group.");
        } catch (TException e) {
            handleException("Unable remove  topic from Endpoint Group ", e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with profile filter entities.
     *
     * @param edit
     *            edit else create profile filter
     * @return the control api command
     */
    private ControlApiCommand createProfileFilterCommand(final boolean edit) {

        ControlApiCommand command = new ControlApiCommand(
                edit ? "editProfileFilter" : "createProfileFilter",
                (edit ? "edit" : CREATE) + PROFILE_FILTER) {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                createProfileFilter(line, client, writer, errorWriter, edit);
            }
        };

        if (edit) {
            Option opt = new Option("i", PROFILE_FILTER_ID, true,
                    PROFILE_FILTER_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("s", PROFILE_SCHEMA_ID, true,
                PROFILE_SCHEMA_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("e", ENDPOINT_GROUP_ID, true,
                ENDPOINT_GROUP_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("f", "file", true, "Profile Filter JSON file");
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the profile filter.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     * @param edit
     *            edit else create profile filter
     */
    private void createProfileFilter(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {

        try {

            ProfileFilterDto profileFilter = null;
            if (edit) {
                String profileFilterId = line.getOptionValue("i");
                profileFilter = toDto(client.getProfileFilter(profileFilterId));
                if (profileFilter == null) {
                    writer.println("Profile Filter with id " + profileFilterId
                            + NOT_FOUND);
                    return;
                }
            } else {
                profileFilter = new ProfileFilterDto();
            }

            if (line.hasOption("f")) {
                String filterFile = line.getOptionValue("f");
                String filter = readFile(filterFile, errorWriter);
                if (filter != null) {
                    profileFilter.setBody(filter);
                } else {
                    return;
                }
            }
            if (line.hasOption("s")) {
                profileFilter.setSchemaId(line.getOptionValue("s"));
            }
            if (line.hasOption("e")) {
                profileFilter.setEndpointGroupId(line.getOptionValue("e"));
            }

            ProfileFilterDto savedProfileFilter = toDto(client
                    .editProfileFilter(toDataStruct(profileFilter)));

            if (edit) {
                writer.println("Profile Filter updated.");
            } else {
                writer.println("Created new Profile Filter with id: "
                        + savedProfileFilter.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedProfileFilter.getId(),
                            errorWriter);
                }

            }

        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + PROFILE_FILTER, e, errorWriter);
        }
    }

    /**
     * Creates API command to activate profile filter.
     *
     * @return the control api command
     */
    private ControlApiCommand activateProfileFilterCommand() {

        ControlApiCommand command = new ControlApiCommand(
                "activateProfileFilter", "activate Profile Filter") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                activateProfileFilter(line, client, writer, errorWriter);
            }
        };

        Option opt = new Option("i", PROFILE_FILTER_ID, true,
                PROFILE_FILTER_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);
        return command;
    }

    /**
     * Activate profile filter.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void activateProfileFilter(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {
        try {
            if (line.hasOption("i")) {
                String profileFilterId = line.getOptionValue("i");
                @SuppressWarnings("unused")
                ProfileFilterDto profileFilter = toDto(client.activateProfileFilter(profileFilterId, null)); //NOSONAR
                writer.println("Profile Filter Activated.");
            }
        } catch (TException e) {
            handleException(UNABLE_TO_ACTIVATE_CONFIGURATION, e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with configuration entities.
     *
     * @param edit
     *            edit else create configuration
     * @return the control api command
     */
    private ControlApiCommand createConfigurationCommand(final boolean edit) {

        ControlApiCommand command = new ControlApiCommand(
                edit ? "editConfiguration" : "createConfiguration",
                (edit ? "edit" : CREATE) + CONFIGURATION) {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                createConfiguration(line, client, writer, errorWriter, edit);
            }
        };

        if (edit) {
            Option opt = new Option("i", CONFIGURATION_ID, true,
                    CONFIGURATION_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("s", CONFIGURATION_SCHEMA_ID, true,
                CONFIGURATION_SCHEMA_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("e", ENDPOINT_GROUP_ID, true,
                ENDPOINT_GROUP_ID_OPTION);
        opt.setRequired(false);
        command.addOption(opt);
        opt = new Option("f", "file", true, "Configuration JSON file");
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the configuration.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     * @param edit
     *            edit else create configuration
     */
    private void createConfiguration(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {
        try {

            ConfigurationDto configuration = null;
            if (edit) {
                String configurationId = line.getOptionValue("i");
                configuration = toDto(client.getConfiguration(configurationId));
                if (configuration == null) {
                    writer.println("Configuration with id " + configurationId
                            + NOT_FOUND);
                    return;
                }
            } else {
                configuration = new ConfigurationDto();
                configuration.setStatus(UpdateStatus.INACTIVE);
            }

            if (line.hasOption("f")) {
                String configFile = line.getOptionValue("f");
                String config = readFile(configFile, errorWriter);
                if (config != null) {
                    configuration.setBody(config);
                } else {
                    return;
                }
            }
            if (line.hasOption("s")) {
                configuration
                        .setSchemaId(line.getOptionValue("s"));
            }
            if (line.hasOption("e")) {
                configuration.setEndpointGroupId(line.getOptionValue("e"));
            }

            ConfigurationDto savedConfiguration = toDto(client
                    .editConfiguration(toDataStruct(configuration)));

            if (edit) {
                writer.println("Configuration updated.");
            } else {
                writer.println("Created new Configuration with id: "
                        + savedConfiguration.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedConfiguration.getId(),
                            errorWriter);
                }

            }

        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + CONFIGURATION, e, errorWriter);
        }
    }

    /**
     * Creates API command to activate configuration.
     *
     * @return the control api command
     */
    private ControlApiCommand activateConfigurationCommand() {

        ControlApiCommand command = new ControlApiCommand(
                "activateConfiguration", "activate Configuration") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                activateConfiguration(line, client, writer, errorWriter);
            }
        };

        Option opt = new Option("i", CONFIGURATION_ID, true,
                CONFIGURATION_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);
        return command;
    }

    /**
     * Activate configuration.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void activateConfiguration(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {
        try {
            if (line.hasOption("i")) {
                String configurationId = line.getOptionValue("i");
                @SuppressWarnings("unused")
                ConfigurationDto configuration = toDto(client //NOSONAR
                        .activateConfiguration(configurationId, null));
                writer.println("Configuration Activated.");
            }
        } catch (TException e) {
            handleException(UNABLE_TO_ACTIVATE_CONFIGURATION, e, errorWriter);
        }
    }

    /**
     * Creates API command to generate sdk.
     *
     * @return the control api command
     */
    private ControlApiCommand generateSdkCommand() {

        ControlApiCommand command = new ControlApiCommand(
                "generateSdk", "generate SDK") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                generateSdk(line, client, writer, errorWriter);
            }
        };

        Option opt = new Option("sdk", "sdkPlatform", true,
                "SDK platform option (java)");
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("a", APPLICATION_ID, true,
                APPLICATION_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("psv", "profileSchemaVersion", true,
                "Profile schema version option");
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("csv", "configurationSchemaVersion", true,
                "Configuration schema version option");
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("nsv", "notificationSchemaVersion", true,
                "Notification schema version option");
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("lsv", "logSchemaVersion", true,
                "Log schema version option");
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("out", "outputDir", true,
                "Output directory to store sdk file");
        command.addOption(opt);

        return command;
    }

    /**
     * Generate sdk.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void generateSdk(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {
        try {
            String sdkPlatformString = line.getOptionValue("sdk");
            SdkPlatform sdkPlatform = SdkPlatform.valueOf(sdkPlatformString.trim().toUpperCase());
            String applicationId = line.getOptionValue("a");
            int profileSchemaVersion = Integer.parseInt(line.getOptionValue("psv"));
            int configurationSchemaVersion = Integer.parseInt(line.getOptionValue("csv"));
            int notificationSchemaVersion = Integer.parseInt(line.getOptionValue("nsv"));
            int logSchemaVersion = Integer.parseInt(line.getOptionValue("lsv"));

            Sdk sdk = client.generateSdk(sdkPlatform,
                    applicationId,
                    profileSchemaVersion,
                    configurationSchemaVersion,
                    notificationSchemaVersion,
                    null,
                    logSchemaVersion);

            writer.println("Generated SDK: " + sdk.getFileName());

            String fileName = sdk.getFileName();

            File outputFile;
            if (line.hasOption("out")) {
                String outDir = line.getOptionValue("out");
                File outputDir = new File(outDir);
                outputDir.mkdirs();
                outputFile = new File(outputDir, fileName);
            } else {
                outputFile = new File(fileName);
            }

            writer.println("Saving SDK to file: " + outputFile.getAbsolutePath());

            try { //NOSONAR
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(sdk.getData());
                fos.flush();
                fos.close();
                writer.println("Saved SDK to file: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                LOG.error("Unable to save SDK file: {}", outputFile.getAbsolutePath());
            }

        } catch (TException e) {
            handleException("Unable to generate SDK", e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with topic entities.
     *
     * @param edit
     *            edit else create topic
     * @return the control api command
     */
    private ControlApiCommand createTopicCommand(final boolean edit) {

        ControlApiCommand command = new ControlApiCommand(
                edit ? "editTopic" : "createTopic",
                (edit ? "edit" : CREATE) + TOPIC) {
            @Override
            public void runCommand(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {
                createTopic(line, client, writer, errorWriter, edit);
            }
        };
        if (edit) {
            Option opt = new Option("i", TOPIC_ID, true,
                    TOPIC_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("n", "name", true, "Topic Name option");
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("a", APPLICATION_ID, true, APPLICATION_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);
        opt = new Option("t", "type", true, "Topic type option. Values: MANDATORY, VOLUNTARY");
        opt.setRequired(!edit);
        command.addOption(opt);
        return command;
    }

    /**
     * Creates or edits the topic.
     *
     * @param line        the command line
     * @param client      the control thrift client
     * @param writer      the writer to output command results
     * @param errorWriter the error writer to output command errors
     * @param edit        edit else create configuration
     */
    private void createTopic(CommandLine line,
                             ControlThriftService.Iface client, PrintWriter writer,
                             PrintWriter errorWriter, boolean edit) {
        try {

            TopicDto topicDto;
            if (edit) {
                String topicId = line.getOptionValue("i");
                topicDto = toDto(client.getTopic(topicId));
                if (topicDto == null) {
                    writer.println("Topic with id " + topicId
                            + NOT_FOUND);
                    return;
                }
            } else {
                topicDto = new TopicDto();
            }

            if (line.hasOption("a")) {
                String applicationId = line.getOptionValue("a");
                if (StringUtils.isNotBlank(applicationId)) {
                    topicDto.setApplicationId(applicationId);
                } else {
                    errorWriter.println("Invalid application id for topic.");
                    return;
                }
            }
            if (line.hasOption("n")) {
                topicDto.setName(line.getOptionValue("n"));
            }
            if (line.hasOption("t")) {
                String type = line.getOptionValue("t");
                topicDto.setType(TopicTypeDto.valueOf(type.toUpperCase()));
            }
            TopicDto savedTopic = toDto(client
                    .editTopic(toDataStruct(topicDto)));
            if (edit) {
                writer.println("Topic updated.");
            } else {
                writer.println("Created new Topic with id: "
                        + savedTopic.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedTopic.getId(),
                            errorWriter);
                }
            }
        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + TOPIC, e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with notification entities.
     *
     * @return the control api command
     */
    private ControlApiCommand createNotificationCommand() {

        ControlApiCommand command = new ControlApiCommand(
                "createNotification",
                CREATE + NOTIFICATION) {
            @Override
            public void runCommand(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {
                createNotification(line, client, writer, errorWriter);
            }
        };

        Option opt = new Option("o", OUTPUT, true,
                OUTPUT_FILE_TO_STORE_OBJECT_ID);
        opt.setRequired(false);
        command.addOption(opt);

        opt = new Option("s", SCHEMA_ID, true, NOTIFICATION_SCHEMA_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("t", TOPIC__ID, true, NOTIFICATION_TOPIC_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("l", "ttl", true, "Time to live in seconds for Notification object option.");
        opt.setRequired(false);
        command.addOption(opt);

        opt = new Option("f", BODY_FILE, true, NOTIFICATION_BODY_FILE_OPTION);
        opt.setRequired(false);
        command.addOption(opt);

        opt = new Option("b", "body", true, NOTIFICATION_BODY_OPTION);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * Creates or edits the notification.
     *
     * @param line        the command line
     * @param client      the control thrift client
     * @param writer      the writer to output command results
     * @param errorWriter the error writer to output command errors
     */
    private void createNotification(CommandLine line,
                                    ControlThriftService.Iface client, PrintWriter writer,
                                    PrintWriter errorWriter) {
        try {
            NotificationDto notificationDto = new NotificationDto();
            if (line.hasOption("s")) {
                String schemaId = line.getOptionValue("s");
                if (StringUtils.isNotBlank(schemaId)) {
                    notificationDto.setSchemaId(schemaId);
                } else {
                    errorWriter.println(INVALID_SCHEMA_ID_FOR_NOTIFICATION);
                    return;
                }
            }
            if (line.hasOption("t")) {
                String topicId = line.getOptionValue("t");
                if (StringUtils.isNotBlank(topicId)) {
                    notificationDto.setTopicId(topicId);
                } else {
                    errorWriter.println(INVALID_TOPIC_ID_FOR_NOTIFICATION);
                    return;
                }
            }
            if (line.hasOption("l")) {
                String ttl = line.getOptionValue("l");
                try { //NOSONAR
                    long time = System.currentTimeMillis() + (Integer.valueOf(ttl) * 1000L);
                    notificationDto.setExpiredAt(new Date(time));
                } catch (NumberFormatException ex) {
                    errorWriter.println(INCORRECT_FORMAT_OF_TTL + ex.getMessage());
                    return;
                }
            }
            if (line.hasOption("f")) {
                String schemaFile = line.getOptionValue("f");
                String schema = readFile(schemaFile, errorWriter);
                if (schema != null) {
                    notificationDto.setBody(schema.getBytes(UTF8));
                } else {
                    errorWriter.println(CANT_READ_FILE);
                    return;
                }
            } else if (line.hasOption("b")) {
                String body = line.getOptionValue("b");
                notificationDto.setBody(body.getBytes(UTF8));
            } else {
                errorWriter.println(NEED_TO_SET_BODY);
                return;
            }
            NotificationDto savedNotification = toDto(client
                    .editNotification(toDataStruct(notificationDto)));
            writer.println("Created new Notification with id: "
                    + savedNotification.getId());
            if (line.hasOption("o")) {
                String outFileName = line.getOptionValue("o");
                storeInfo(outFileName, savedNotification.getId(),
                        errorWriter);
            }
        } catch (TException e) {
            handleException(UNABLE_TO + CREATE + NOTIFICATION, e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with notification entities.
     *
     * @return the control api command
     */
    private ControlApiCommand createUnicastNotificationCommand() {

        ControlApiCommand command = new ControlApiCommand("createUnicastNotification", CREATE + NOTIFICATION) {
            @Override
            public void runCommand(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {
                createUnicastNotification(line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("o", OUTPUT, true,
                OUTPUT_FILE_TO_STORE_OBJECT_ID);
        opt.setRequired(false);
        command.addOption(opt);

        opt = new Option("k", "key-hash", true, "Unicast Notification key hash option");
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("s", SCHEMA_ID, true, NOTIFICATION_SCHEMA_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("t", TOPIC__ID, true, NOTIFICATION_TOPIC_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("l", "ttl", true, "Time to live in seconds for Unicast Notification object option.");
        opt.setRequired(false);
        command.addOption(opt);

        opt = new Option("f", BODY_FILE, true, NOTIFICATION_BODY_FILE_OPTION);
        opt.setRequired(false);
        command.addOption(opt);

        opt = new Option("b", "body", true, NOTIFICATION_BODY_OPTION);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * Creates or edits the notification.
     *
     * @param line        the command line
     * @param client      the control thrift client
     * @param writer      the writer to output command results
     * @param errorWriter the error writer to output command errors
     */
    private void createUnicastNotification(CommandLine line,
                                    ControlThriftService.Iface client, PrintWriter writer,
                                    PrintWriter errorWriter) {
        try {
            NotificationDto notification = new NotificationDto();
            EndpointNotificationDto endpointNotification = new EndpointNotificationDto();
            endpointNotification.setNotificationDto(notification);
            if (line.hasOption("t")) {
                String topicId = line.getOptionValue("t");
                if (StringUtils.isNotBlank(topicId)) {
                    notification.setTopicId(topicId);
                } else {
                    errorWriter.println(INVALID_TOPIC_ID_FOR_NOTIFICATION);
                    return;
                }
            }
            if (line.hasOption("s")) {
                String schemaId = line.getOptionValue("s");
                if (StringUtils.isNotBlank(schemaId)) {
                    notification.setSchemaId(schemaId);
                } else {
                    errorWriter.println(INVALID_SCHEMA_ID_FOR_NOTIFICATION);
                    return;
                }
            }
            if (line.hasOption("k")) {
                String keyHash = line.getOptionValue("k");
                if (StringUtils.isNotBlank(keyHash)) {
                    endpointNotification.setEndpointKeyHash(Base64.decodeBase64(keyHash));
                } else {
                    errorWriter.println("Empty key hash for unicast notification.");
                    return;
                }
            }
            if (line.hasOption("l")) {
                String ttl = line.getOptionValue("l");
                try { //NOSONAR
                    long time = System.currentTimeMillis() + (Integer.valueOf(ttl) * 1000L);
                    notification.setExpiredAt(new Date(time));
                } catch (NumberFormatException ex) {
                    errorWriter.println(INCORRECT_FORMAT_OF_TTL + ex.getMessage());
                    return;
                }
            }
            if (line.hasOption("f")) {
                String schemaFile = line.getOptionValue("f");
                String schema = readFile(schemaFile, errorWriter);
                if (schema != null) {
                    notification.setBody(schema.getBytes(UTF8));
                } else {
                    errorWriter.println(CANT_READ_FILE);
                    return;
                }
            } else if (line.hasOption("b")) {
                String body = line.getOptionValue("b");
                notification.setBody(body.getBytes(UTF8));
            } else {
                errorWriter.println(NEED_TO_SET_BODY);
                return;
            }
            EndpointNotificationDto savedNotification = toDto(client
                    .editUnicastNotification(toDataStruct(endpointNotification)));
            writer.println("Created new Unicast Notification with id: "
                    + savedNotification.getId());
            if (line.hasOption("o")) {
                String outFileName = line.getOptionValue("o");
                storeInfo(outFileName, savedNotification.getId(),
                        errorWriter);
            }
        } catch (TException e) {
            handleException(UNABLE_TO + CREATE + " Unicast Notification", e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with notification entities.
     *
     * @param edit edit else create topic
     * @return the control api command
     */
    private ControlApiCommand createNotificationSchemaCommand(final boolean edit) {

        ControlApiCommand command = new ControlApiCommand(
                edit ? "editNotificationSchema" : "createNotificationSchema",
                (edit ? "edit" : CREATE) + NOTIFICATION) {
            @Override
            public void runCommand(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {
                createNotificationSchema(line, client, writer, errorWriter, edit);
            }
        };
        if (edit) {
            Option opt = new Option("i", "notificationSchemaId", true,
                    NOTIFICATION_SCHEMA_ID_OPTION);
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", OUTPUT, true,
                    OUTPUT_FILE_TO_STORE_OBJECT_ID);
            opt.setRequired(false);
            command.addOption(opt);
        }
        Option opt = new Option("a", APPLICATION_ID, true, APPLICATION_ID_OPTION);
        opt.setRequired(!edit);
        command.addOption(opt);

        opt = new Option("t", "type", true, "Notification Schema type option. Values: USER, SYSTEM");
        opt.setRequired(!edit);
        command.addOption(opt);

        opt = new Option("f", BODY_FILE, true, "Notification Schema body file option.");
        opt.setRequired(false);
        command.addOption(opt);

        opt = new Option("b", "body", true, "Notification Schema body option.");
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * Creates or edits the notification.
     *
     * @param line        the command line
     * @param client      the control thrift client
     * @param writer      the writer to output command results
     * @param errorWriter the error writer to output command errors
     * @param edit        edit else create configuration
     */
    private void createNotificationSchema(CommandLine line,
                                    ControlThriftService.Iface client, PrintWriter writer,
                                    PrintWriter errorWriter, boolean edit) {
        try {
            NotificationSchemaDto notificationSchemaDto;
            if (edit) {
                String notificationSchemaId = line.getOptionValue("i");
                notificationSchemaDto = toDto(client.getNotificationSchema(notificationSchemaId));
                if (notificationSchemaDto == null) {
                    writer.println("Notification Schema with id " + notificationSchemaId + NOT_FOUND);
                    return;
                }
            } else {
                notificationSchemaDto = new NotificationSchemaDto();
            }
            if (line.hasOption("a")) {
                String applicationId = line.getOptionValue("a");
                if (StringUtils.isNotBlank(applicationId)) {
                    notificationSchemaDto.setApplicationId(applicationId);
                } else {
                    errorWriter.println("Invalid application id for notification.");
                    return;
                }
            }
            if (line.hasOption("t")) {
                String type = line.getOptionValue("t");
                if (StringUtils.isNotBlank(type)) {
                    NotificationTypeDto typeDto;
                    try { //NOSONAR
                        typeDto = NotificationTypeDto.valueOf(type);
                    } catch (IllegalArgumentException ex) {
                        LOG.error("Incorrect type of notification {}", ex);
                        return;
                    }
                    notificationSchemaDto.setType(typeDto);
                } else {
                    errorWriter.println("Empty type of notification");
                    return;
                }
            }
            if (line.hasOption("f")) {
                String schemaFile = line.getOptionValue("f");
                String schema = readFile(schemaFile, errorWriter);
                if (StringUtils.isNotBlank(schema)) {
                    notificationSchemaDto.setSchema(new KaaSchemaFactoryImpl().createDataSchema(schema).getRawSchema());
                } else {
                    errorWriter.println(CANT_READ_FILE);
                    return;
                }
            } else if (line.hasOption("b")) {
                String body = line.getOptionValue("b");
                notificationSchemaDto.setSchema(new KaaSchemaFactoryImpl().createDataSchema(body).getRawSchema());
            } else {
                errorWriter.println("Need to set body or file with body for notification schema");
                return;
            }

            NotificationSchemaDto savedNotificationSchema = toDto(client
                    .editNotificationSchema(toDataStruct(notificationSchemaDto)));
            if (edit) {
                writer.println("Notification Schema updated.");
            } else {
                writer.println("Created new Notification Schema with id: "
                        + savedNotificationSchema.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedNotificationSchema.getId(),
                            errorWriter);
                }
            }
        } catch (TException e) {
            handleException(UNABLE_TO + (edit ? UPDATE : CREATE)
                    + NOTIFICATION, e, errorWriter);
        }
    }

    /**
     * Creates the API command to work with endpoint user entities.
     *
     * @param edit edit else create endpoint user
     * @return the control api command
     */
    private ControlApiCommand createEndpointUserCommand(final boolean edit) {
        ControlApiCommand command = new ControlApiCommand(edit ? "editEndpointUser"
                : "createEndpointUser", (edit ? "edit" : "create") + " EndpointUser") {
                    @Override
                    public void runCommand(CommandLine line,
                            ControlThriftService.Iface client, PrintWriter writer,
                            PrintWriter errorWriter) {
                        createEndpointUser(line, client, writer, errorWriter, edit);
                    }
                };
        if (edit) {
            Option opt = new Option("i", "endpointUserId", true,
                    "Endpoint User Id option");
            opt.setRequired(true);
            command.addOption(opt);
        } else {
            Option opt = new Option("o", "output", true,
                    "Output file to store Object Id");
            opt.setRequired(false);
            command.addOption(opt);
        }

        Option opt = new Option("n", "name", true, "Endpoint User Name option");
        opt.setRequired(!edit);
        command.addOption(opt);

        opt = new Option("t", "tenantId", true, "Tenant Id option");
        opt.setRequired(!edit);
        command.addOption(opt);

        opt = new Option("e", "externalId", true, "Endpoint User External Id option");
        opt.setRequired(false);
        command.addOption(opt);

        opt = new Option("a", "accessToken", true, "Endpoint User Access Token option");
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * Creates or edits the endpoint user.
     *
     * @param line the command line
     * @param client the control thrift client
     * @param writer the writer to output command results
     * @param errorWriter the error writer to output command errors
     * @param edit edit else create endpoint user
     */
    private void createEndpointUser(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter, boolean edit) {
        try {
            EndpointUserDto endpointUser;
            if (edit) {
                String endpointUserId = line.getOptionValue("i");
                endpointUser = toDto(client.getEndpointUser(endpointUserId));
                if (endpointUser == null) {
                    writer.println("Endpoint user with id " + endpointUserId
                            + " not found!");
                    return;
                }
            } else {
                endpointUser = new EndpointUserDto();
            }
            if (line.hasOption("n")) {
                endpointUser.setUsername(line.getOptionValue("n"));
            }
            if (line.hasOption("t")) {
                endpointUser.setTenantId(line.getOptionValue("t"));
            }
            if (line.hasOption("e")) {
                endpointUser.setExternalId(line.getOptionValue("e"));
            }
            if (line.hasOption("a")) {
                endpointUser.setAccessToken(line.getOptionValue("a"));
            }
            EndpointUserDto savedEndpointUser = toDto(client
                    .editEndpointUser(toDataStruct(endpointUser)));
            if (edit) {
                writer.println("Endpoint user updated.");
            } else {
                writer.println("Created new endpoint user with id: "
                        + savedEndpointUser.getId());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeInfo(outFileName, savedEndpointUser.getId(),
                            errorWriter);
                }
            }
        } catch (TException e) {
            handleException("Unable to " + (edit ? "update" : "create")
                    + " endpoint user", e, errorWriter);
        }
    }



    /**
     * Creates show command used to display existing entity.
     *
     * @param type
     *            the entity type to display
     * @return the control api command
     */
    private ControlApiCommand showCommand(final EntityType type) {
        ControlApiCommand command = new ControlApiCommand("show"
                + type.getName(), "show " + type.getName()) {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                show(type, line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("i", "id", true, type.getName() + ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);
        return command;
    }

    /**
     * Display existing entity.
     *
     * @param type
     *            the entity type to display
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void show(EntityType type, CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {
        if (line.hasOption("i")) {
            String id = line.getOptionValue("i");
            HasId dto = null;
            try {
                switch (type) {
                    case TENANT:
                        dto = ThriftDtoConverter.<TenantDto> toDto(client
                                .getTenant(id));
                        break;
                    case USER:
                        dto = ThriftDtoConverter
                                .<UserDto> toDto(client.getUser(id));
                        break;
                    case APPLICATION:
                        dto = ThriftDtoConverter.<ApplicationDto> toDto(client
                                .getApplication(id));
                        break;
                    case PROFILE_SCHEMA:
                        dto = ThriftDtoConverter.<ProfileSchemaDto> toDto(client
                                .getProfileSchema(id));
                        break;
                    case CONFIGURATION_SCHEMA:
                        dto = ThriftDtoConverter
                                .<ConfigurationSchemaDto> toDto(client
                                        .getConfigurationSchema(id));
                        break;
                    case LOG_SCHEMA:
                        dto = ThriftDtoConverter
                                .<LogSchemaDto> toDto(client
                                        .getLogSchema(id));
                        break;
                    case ENDPOINT_GROUP:
                        dto = ThriftDtoConverter.<EndpointGroupDto> toDto(client
                                .getEndpointGroup(id));
                        break;
                    case PROFILE_FILTER:
                        dto = ThriftDtoConverter.<ProfileFilterDto> toDto(client
                                .getProfileFilter(id));
                        break;
                    case CONFIGURATION:
                        dto = ThriftDtoConverter.<ConfigurationDto> toDto(client
                                .getConfiguration(id));
                        break;
                    case NOTIFICATION:
                        dto = ThriftDtoConverter.<ConfigurationDto> toDto(client
                                .getNotification(id));
                        break;
                    case PERSONAL_NOTIFICATION:
                        dto = ThriftDtoConverter.<ConfigurationDto> toDto(client
                                .getUnicastNotification(id));
                        break;
                    case NOTIFICATION_SCHEMA:
                        dto = ThriftDtoConverter.<ConfigurationDto> toDto(client
                                .getNotificationSchema(id));
                        break;
                    case TOPIC:
                        dto = ThriftDtoConverter.<ConfigurationDto> toDto(client
                                .getTopic(id));
                        break;
                    case ENDPOINT_USER:
                        dto = ThriftDtoConverter.<ConfigurationDto> toDto(client
                                .getEndpointUser(id));
                        break;
                    default:
                        break;
                }
                if (dto != null) {
                    writer.println("Found " + type.getName() + ":");
                    writer.println(dto);
                } else {
                    writer.println(type.getName() + " with id " + id
                            + NOT_FOUND);
                }

            } catch (TException e) {
                handleException("Unable to show " + type.getName(), e,
                        errorWriter);
            }
        } else {
            errorWriter.println("Error: " + type.getName()
                    + " Id option is missing!");
        }
    }

    /**
     * Creates list command used to display list of existing entities.
     *
     * @param type
     *            the entity type to display
     * @return the control api command
     */
    private ControlApiCommand listCommand(final EntityType type) {
        ControlApiCommand command = new ControlApiCommand("list"
                + type.getName(), "list " + type.getName() + "s") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                list(type, line, client, writer, errorWriter);
            }
        };

        Option opt = new Option("o", OUTPUT, true,
                OUTPUT_FILE_TO_STORE_IDS);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * Display list of existing entities.
     *
     * @param type
     *            the entity type to display
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void list(EntityType type, CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {

        List<? extends HasId> dtos = null;
        try {
            switch (type) {
            case TENANT:
                dtos = ThriftDtoConverter.<TenantDto> toDtoList(client
                        .getTenants());
                break;
            case USER:
                dtos = ThriftDtoConverter
                        .<UserDto> toDtoList(client.getUsers());
                break;
            case ENDPOINT_USER:
                dtos = ThriftDtoConverter
                        .<UserDto> toDtoList(client.getEndpointUsers());
                break;
            default:
                break;
            }
            if (dtos == null) {
                writer.println("Not implemented!");
            } else {
                writer.println("List of " + type.getName() + "s:");
                writer.println();
                for (HasId dto : dtos) {
                    writer.println(dto);
                }
                writer.println();
                writer.println(TOTAL + dtos.size());
                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeObjectIds(outFileName, dtos, errorWriter);
                }

            }
        } catch (TException e) {
            handleException("Unable to get list of " + type.getName(), e,
                    errorWriter);
        }
    }

    /**
     * Creates API command to list existing applications.
     *
     * @return the control api command
     */
    private ControlApiCommand listApplicationsCommand() {
        ControlApiCommand command = new ControlApiCommand("listApplications",
                "list Applications by Tenant Id") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                listApplications(line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("t", TENANT_ID, true, TENANT_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("o", OUTPUT, true, OUTPUT_FILE_TO_STORE_IDS);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * List existing applications.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void listApplications(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {

        if (line.hasOption("t")) {
            try {
                String tentantId = line.getOptionValue("t");
                List<? extends HasId> dtos = ThriftDtoConverter
                        .<ApplicationDto> toDtoList(client
                                .getApplicationsByTenantId(tentantId));
                writer.println("List of Applications:");
                writer.println();
                for (HasId dto : dtos) {
                    writer.println(dto);
                }
                writer.println();
                writer.println(TOTAL + dtos.size());

                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeObjectIds(outFileName, dtos, errorWriter);
                }
            } catch (TException e) {
                handleException("Unable to get list of Applications", e,
                        errorWriter);
            }
        }
    }

    /**
     * Creates API command to list existing profile schemas.
     *
     * @return the control api command
     */
    private ControlApiCommand listProfileSchemasCommand() {
        ControlApiCommand command = new ControlApiCommand("listProfileSchemas",
                "list Profile Schemas by Application Id") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                listProfileSchemas(line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("a", APPLICATION_ID, true,
                APPLICATION_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("o", OUTPUT, true, OUTPUT_FILE_TO_STORE_IDS);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * List existing profile schemas.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void listProfileSchemas(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {

        if (line.hasOption("a")) {
            try {
                String applicationId = line.getOptionValue("a");
                List<? extends HasId> dtos = ThriftDtoConverter
                        .<ProfileSchemaDto> toDtoList(client
                                .getProfileSchemasByApplicationId(applicationId));
                writer.println("List of Profile Schemas:");
                writer.println();
                for (HasId dto : dtos) {
                    writer.println(dto);
                }
                writer.println();
                writer.println(TOTAL + dtos.size());

                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeObjectIds(outFileName, dtos, errorWriter);
                }
            } catch (TException e) {
                handleException("Unable to get list of Profile Schemas", e,
                        errorWriter);
            }
        }
    }

    /**
     * Creates API command to list existing configuration schemas.
     *
     * @return the control api command
     */
    private ControlApiCommand listConfigurationSchemasCommand() {
        ControlApiCommand command = new ControlApiCommand(
                "listConfigurationSchemas",
                "list Configuration Schemas by Application Id") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                listConfigurationSchemas(line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("a", APPLICATION_ID, true,
                APPLICATION_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("o", OUTPUT, true, OUTPUT_FILE_TO_STORE_IDS);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * List existing configuration schemas.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void listConfigurationSchemas(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {

        if (line.hasOption("a")) {
            try {
                String applicationId = line.getOptionValue("a");
                List<? extends HasId> dtos = ThriftDtoConverter
                        .<ConfigurationSchemaDto> toDtoList(client
                                .getConfigurationSchemasByApplicationId(applicationId));
                writer.println("List of Configuration Schemas:");
                writer.println();
                for (HasId dto : dtos) {
                    writer.println(dto);
                }
                writer.println();
                writer.println(TOTAL + dtos.size());

                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeObjectIds(outFileName, dtos, errorWriter);
                }
            } catch (TException e) {
                handleException("Unable to get list of Configuration Schemas",
                        e, errorWriter);
            }
        }
    }

    /**
     * Creates API command to list existing endpoint groups.
     *
     * @return the control api command
     */
    private ControlApiCommand listEndpointGroupsCommand() {
        ControlApiCommand command = new ControlApiCommand("listEndpointGroups",
                "list Endpoint Groups by Application Id") {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                listEndpointGroups(line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("a", APPLICATION_ID, true,
                APPLICATION_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("o", OUTPUT, true, OUTPUT_FILE_TO_STORE_IDS);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * List existing endpoint groups.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void listEndpointGroups(CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {

        if (line.hasOption("a")) {
            try {
                String applicationId = line.getOptionValue("a");
                List<? extends HasId> dtos = ThriftDtoConverter
                        .<EndpointGroupDto> toDtoList(client
                                .getEndpointGroupsByApplicationId(applicationId));
                writer.println("List of Endpoint Groups:");
                writer.println();
                for (HasId dto : dtos) {
                    writer.println(dto);
                }
                writer.println();
                writer.println(TOTAL + dtos.size());

                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeObjectIds(outFileName, dtos, errorWriter);
                }
            } catch (TException e) {
                handleException("Unable to get list of Endpoint Groups", e,
                        errorWriter);
            }
        }
    }

    /**
     * Creates API command to list existing topics.
     *
     * @return the control api command
     */
    private ControlApiCommand listTopicsCommand() {
        ControlApiCommand command = new ControlApiCommand("listTopics",
                "list Topics by Application Id") {
            @Override
            public void runCommand(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {
                listTopics(line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("a", APPLICATION_ID, true, APPLICATION_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("o", OUTPUT, true, OUTPUT_FILE_TO_STORE_IDS);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * List existing topics.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void listTopics(CommandLine line,
                                  ControlThriftService.Iface client, PrintWriter writer,
                                  PrintWriter errorWriter) {

        if (line.hasOption("a")) {
            try {
                String appId = line.getOptionValue("a");
                List<? extends HasId> dtos = ThriftDtoConverter
                        .<TopicDto> toDtoList(client
                                .getTopicByAppId(appId));
                writer.println("List of Topics:");
                writer.println();
                for (HasId dto : dtos) {
                    writer.println(dto);
                }
                writer.println();
                writer.println(TOTAL + dtos.size());

                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeObjectIds(outFileName, dtos, errorWriter);
                }
            } catch (TException e) {
                handleException("Unable to get list of Topics", e,
                        errorWriter);
            }
        }
    }

    /**
     * Creates API command to list existing notifications.
     *
     * @return the control api command
     */
    private ControlApiCommand listNotificationsCommand() {
        ControlApiCommand command = new ControlApiCommand("listNotifications",
                "list notifications by topic Id") {
            @Override
            public void runCommand(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {
                listNotifications(line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("t", TOPIC_ID, true, TOPIC_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("o", OUTPUT, true, OUTPUT_FILE_TO_STORE_IDS);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * List existing notifications.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void listNotifications(CommandLine line,
                            ControlThriftService.Iface client, PrintWriter writer,
                            PrintWriter errorWriter) {

        if (line.hasOption("t")) {
            try {
                String topicId = line.getOptionValue("t");
                List<? extends HasId> dtos = ThriftDtoConverter
                        .<NotificationDto> toDtoList(client
                                .getNotificationsByTopicId(topicId));
                writer.println("List of Notifications:");
                writer.println();
                for (HasId dto : dtos) {
                    writer.println(dto);
                }
                writer.println();
                writer.println(TOTAL + dtos.size());

                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeObjectIds(outFileName, dtos, errorWriter);
                }
            } catch (TException e) {
                handleException("Unable to get list of Notifications", e,
                        errorWriter);
            }
        }
    }

    /**
     * Creates API command to list existing notification schemas.
     *
     * @return the control api command
     */
    private ControlApiCommand listNotificationSchemasCommand() {
        ControlApiCommand command = new ControlApiCommand("listNotificationSchemas",
                "list notifications schemas by application Id") {
            @Override
            public void runCommand(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {
                listNotificationSchemas(line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("a", APPLICATION_ID, true, APPLICATION_ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);

        opt = new Option("o", OUTPUT, true, OUTPUT_FILE_TO_STORE_IDS);
        opt.setRequired(false);
        command.addOption(opt);

        return command;
    }

    /**
     * List existing notification schemas.
     *
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void listNotificationSchemas(CommandLine line,
                                   ControlThriftService.Iface client, PrintWriter writer,
                                   PrintWriter errorWriter) {

        if (line.hasOption("a")) {
            try {
                String appId = line.getOptionValue("a");
                List<? extends HasId> dtos = ThriftDtoConverter
                        .<NotificationSchemaDto> toDtoList(client
                                .getNotificationSchemasByAppId(appId));
                writer.println("List of Notification schemas:");
                writer.println();
                for (HasId dto : dtos) {
                    writer.println(dto);
                }
                writer.println();
                writer.println(TOTAL + dtos.size());

                if (line.hasOption("o")) {
                    String outFileName = line.getOptionValue("o");
                    storeObjectIds(outFileName, dtos, errorWriter);
                }
            } catch (TException e) {
                handleException("Unable to get list of Notification Schemas", e,
                        errorWriter);
            }
        }
    }

    /**
     * Creates API command to delete entity.
     *
     * @param type
     *            the entity type to delete
     * @return the control api command
     */
    private ControlApiCommand deleteCommand(final EntityType type) {
        ControlApiCommand command = new ControlApiCommand("delete"
                + type.getName(), "delete " + type.getName()) {
            @Override
            public void runCommand(CommandLine line,
                    ControlThriftService.Iface client, PrintWriter writer,
                    PrintWriter errorWriter) {
                delete(type, line, client, writer, errorWriter);
            }
        };
        Option opt = new Option("i", "id", true, type.getName() + ID_OPTION);
        opt.setRequired(true);
        command.addOption(opt);
        return command;
    }

    /**
     * Delete entity.
     *
     * @param type
     *            the entity type to delete
     * @param line
     *            the command line
     * @param client
     *            the control thrift client
     * @param writer
     *            the writer to output command results
     * @param errorWriter
     *            the error writer to output command errors
     */
    private void delete(EntityType type, CommandLine line,
            ControlThriftService.Iface client, PrintWriter writer,
            PrintWriter errorWriter) {
            String id = line.getOptionValue("i");
            boolean supported = true;
            try {
                switch (type) {
                case TENANT:
                    client.deleteTenant(id);
                    break;
                case USER:
                    client.deleteUser(id);
                    break;
                case APPLICATION:
                    client.deleteApplication(id);
                    break;
                case ENDPOINT_GROUP:
                    client.deleteEndpointGroup(id);
                    break;
                case TOPIC:
                    client.deleteTopicById(id);
                    break;
                case ENDPOINT_USER:
                    client.deleteEndpointUser(id);
                    break;
                default:
                    errorWriter.println("Command not supported!");
                    supported = false;
                    break;
                }
                if (supported) {
                    writer.println("Deleted " + type.getName() + " with id: " + id);
                }
            } catch (TException e) {
                handleException("Unable to delete " + type.getName(), e,
                        errorWriter);
            }
    }

    /**
     * Read input file and return file contents as string.
     *
     * @param file
     *            the input file to read
     * @param errorWriter
     *            the error writer to output read errors
     * @return the file contents string
     */
    private String readFile(String file, PrintWriter errorWriter) {
        String result = null;

        File f = new File(file);
        if (f.exists() && f.isFile()) {
            try {
                StringBuffer fileData = new StringBuffer();
                BufferedReader reader = new BufferedReader(new FileReader(f));
                char[] buf = new char[1024];
                int numRead = 0;
                while ((numRead = reader.read(buf)) != -1) {
                    String readData = String.valueOf(buf, 0, numRead);
                    fileData.append(readData);
                }
                reader.close();
                result = fileData.toString();
            } catch (FileNotFoundException e) {
                LOG.error("Unable to locate specified file '{}'!", file);
            } catch (IOException e) {
                LOG.error("Unable to read from specified file '{}'! Error:  {}", file, e.getMessage());
                e.printStackTrace(errorWriter); //NOSONAR
            }
        } else if (!f.exists()) {
            errorWriter.println(SPECIFIED_FILE + file
                    + "' does not exists!");
        } else if (!f.isFile()) {
            errorWriter.println(SPECIFIED_FILE + file + "' is not a file!");
        }
        return result;
    }

    /**
     * Store entity info to file.
     *
     * @param file
     *            the target file to store object id
     * @param info
     *            the entity info
     * @param errorWriter
     *            the error writer to output store errors
     */
    private void storeInfo(String file, String info,
            PrintWriter errorWriter) {
        try {
            File f = new File(file);
            f.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.append(info);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            LOG.error("Unable to write Object Id to specified file '{}'! Error: {}", file, e.getMessage());
        }
    }

    /**
     * Store entities object ids to file.
     *
     * @param file
     *            the target file to store object ids
     * @param objects
     *            the entities object ids
     * @param errorWriter
     *            the error writer to output store errors
     */
    private void storeObjectIds(String file, List<? extends HasId> objects,
            PrintWriter errorWriter) {
        try {
            File f = new File(file);
            f.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            for (int i = 0; i < objects.size(); i++) {
                if (i > 0) {
                    writer.newLine();
                }
                writer.append(objects.get(i).getId());
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            LOG.error("Unable to write Object Ids to specified file '{}'! Error: {}", file, e.getMessage());
        }
    }

    /**
     * Handle generic command exception and print resulting error to error
     * writer.
     *
     * @param message
     *            the error message to print out
     * @param t
     *            the throwable to handle
     * @param errorWriter
     *            the error writer to output error information
     */
    private void handleException(String message, Throwable t,
            PrintWriter errorWriter) {
        errorWriter.println(message + ". Error: " + t.getMessage());
        t.printStackTrace(errorWriter); //NOSONAR
    }

}
