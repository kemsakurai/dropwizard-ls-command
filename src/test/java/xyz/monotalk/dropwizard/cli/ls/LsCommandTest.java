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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * LsCommandTest
 *
 * @author Kem
 */
public class LsCommandTest {

    private static class MyApplication extends Application<Configuration> {

        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addCommand(new LsCommand(this));
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
    private final MyApplication application = new MyApplication();
    private final LsCommand<Configuration> command = new LsCommand<>(application);

    @SuppressWarnings("unchecked")
    private final Bootstrap<Configuration> bootstrap = mock(Bootstrap.class);
    private final Namespace namespace = mock(Namespace.class);
    private final Configuration configuration = mock(Configuration.class);

    @Test
    public void hasAName() throws Exception {
        assertThat(command.getName())
                .isEqualTo("ls");
    }

    @Test
    public void hasADescription() throws Exception {
        assertThat(command.getDescription())
                .isEqualTo("Output subCommands's infomation to console");
    }

    @Test
    public void doesNotInteractWithAnything() throws Exception {
        List<Command> commands = Lists.newArrayList(new LsCommand(application));
        when(bootstrap.getCommands()).thenReturn(ImmutableList.copyOf(commands));
        command.run(bootstrap, namespace, configuration);
        verifyZeroInteractions(namespace, configuration);
    }
}
