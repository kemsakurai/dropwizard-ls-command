/*
 * Copyright 2015 Kem
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.monotalk.dropwizard.cli.ls;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.dropwizard.Application;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.Configuration;
import io.dropwizard.cli.CheckCommand;
import io.dropwizard.cli.Cli;
import io.dropwizard.cli.Command;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.util.JarLocation;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.fusesource.jansi.AnsiConsole;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;
import static xyz.monotalk.dropwizard.cli.ls.Strs.INDENT;
import static xyz.monotalk.dropwizard.cli.ls.Strs.INDENT_ASTAH;

/**
 * LsCommand
 *
 * @author Kem
 * @param <T> Application Configruation
 */
public class LsCommand<T extends Configuration> extends ConfiguredCommand<T> {

    private final Class<T> configurationClass;

    private final String applicationName;

    private final Set<Class<? extends Command>> defaultCommands = new HashSet<Class<? extends Command>>() {
        private static final long serialVersionUID = -2855157286174568533L;

        {
            add(ServerCommand.class);
            add(CheckCommand.class);
            add(LsCommand.class);
        }
    };
    private final PrintWriter stdOut;

    public LsCommand(Application<T> application) {
        super("ls", "Output subCommands's infomation to console and exit");
        this.configurationClass = application.getConfigurationClass();
        this.applicationName = application.getName();
        this.stdOut = new PrintWriter(new OutputStreamWriter(System.out, Charsets.UTF_8), true);
    }

    /*
     * Since we don't subclass CheckCommand, we need a concrete reference to the configuration
     * class.
     */
    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
        // Add -l Option
        subparser.addArgument("-l")
                .action(Arguments.storeTrue())
                .help("List in long format. If the output is to a terminal, command's helps is output after command description");
    }

    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        // --------------------------------------------
        // AnsiConsole.systemInstall()
        // -----------------
        AnsiConsole.systemInstall();

        Boolean hasLOptinon = namespace.getBoolean("l");
        if (hasLOptinon == null) {
            hasLOptinon = false;
        }

        List<Command> commands = bootstrap.getCommands();
        Set<CommandString> commandStrings = new TreeSet<>();
        commands.stream().forEach((command) -> {
            CommandString commandString = new CommandString();
            commandString.setName(command.getName());
            commandString.setDescription(command.getDescription());
            CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
            String category = null;
            if (info != null) {
                category = info.category();
            }
            if (Strings.isNullOrEmpty(category)) {
                if (defaultCommands.contains(command.getClass())) {
                    category = Strs.CATEGORY_DEFAULT;
                } else {
                    category = applicationName;
                }
            }
            commandString.setCategory(category);
            commandStrings.add(commandString);
        });
        stdOut.println();
        stdOut.println("available commands:");

        String previousCategory = null;
        for (CommandString elem : commandStrings) {
            if (!elem.getCategory().equals(previousCategory)) {
                String currentCategory = elem.getCategory();
                stdOut.println();
                stdOut.println(ansi().fg(RED).a("[" + currentCategory + "]").reset());
                previousCategory = currentCategory;

            }

            stdOut.println(INDENT_ASTAH + elem.getName() + " : " + ansi().fg(BLUE).a(elem.getDescription()).reset());

            if (hasLOptinon) {
                ByteArrayOutputStream bosOut = new ByteArrayOutputStream();
                PrintStream psOut = new PrintStream(bosOut);
                ByteArrayOutputStream bosErr = new ByteArrayOutputStream();
                PrintStream psErr = new PrintStream(bosErr);
                // execute Cli#run()
                final Cli cli = new Cli(new JarLocation(getClass()), bootstrap, psOut, psErr);
                cli.run(elem.getName(), "-h");
                ByteArrayDataInput input = ByteStreams.newDataInput(bosOut.toString("UTF-8").getBytes());

                stdOut.println();
                stdOut.println("/==========================================================:/");

                String line = input.readLine();
                while (line != null) {
                    stdOut.println(INDENT + INDENT + line);
                    line = input.readLine();
                }
                stdOut.println("/==========================================================:/");
                stdOut.println();
            }
        }
        stdOut.println();

        // --------------------------------------------
        // AnsiConsole.systemUninstall()
        // -----------------
        AnsiConsole.systemUninstall();
    }

    static class CommandString implements Comparable<CommandString> {

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
        private String name;
        private String description;
        private String category;

        @Override
        public int compareTo(CommandString o) {

            int result = this.getCategory().compareTo(o.getCategory());
            if (result != 0) {
                return result;
            }
            result = this.getName().compareTo(o.getName());
            if (result != 0) {
                return result;
            }
            result = this.getDescription().compareTo(o.getDescription());
            return result;
        }
    }
}
