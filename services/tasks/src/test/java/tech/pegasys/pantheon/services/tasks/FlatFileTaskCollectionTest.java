/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.services.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FlatFileTaskCollectionTest
    extends AbstractTaskQueueTest<FlatFileTaskCollection<BytesValue>> {

  private static final int ROLL_SIZE = 10;
  @Rule public final TemporaryFolder folder = new TemporaryFolder();

  @Override
  protected FlatFileTaskCollection<BytesValue> createQueue() throws IOException {
    final Path dataDir = folder.newFolder().toPath();
    return createQueue(dataDir);
  }

  private FlatFileTaskCollection<BytesValue> createQueue(final Path dataDir) {
    return new FlatFileTaskCollection<>(
        dataDir, Function.identity(), Function.identity(), ROLL_SIZE);
  }

  @Test
  public void shouldRollFilesWhenSizeExceeded() throws Exception {
    final Path dataDir = folder.newFolder().toPath();
    try (final FlatFileTaskCollection<BytesValue> queue = createQueue(dataDir)) {
      final List<BytesValue> tasks = new ArrayList<>();

      addItem(queue, tasks, 0);
      final File[] currentFiles = getCurrentFiles(dataDir);
      assertThat(currentFiles).hasSize(1);
      final File firstFile = currentFiles[0];
      int tasksInFirstFile = 1;
      while (getCurrentFiles(dataDir).length == 1) {
        addItem(queue, tasks, tasksInFirstFile);
        tasksInFirstFile++;
      }

      assertThat(getCurrentFiles(dataDir)).hasSizeGreaterThan(1);
      assertThat(getCurrentFiles(dataDir)).contains(firstFile);

      // Add an extra item to be sure we have at least one in a later file
      addItem(queue, tasks, 123);

      final List<BytesValue> removedTasks = new ArrayList<>();
      // Read through all the items in the first file.
      for (int i = 0; i < tasksInFirstFile; i++) {
        removedTasks.add(queue.remove().getData());
      }

      // Fully read files should have been removed.
      assertThat(getCurrentFiles(dataDir)).doesNotContain(firstFile);

      // Check that all tasks were read correctly.
      removedTasks.add(queue.remove().getData());
      assertThat(queue.isEmpty()).isTrue();
      assertThat(removedTasks).isEqualTo(tasks);
    }
  }

  private void addItem(
      final FlatFileTaskCollection<BytesValue> queue,
      final List<BytesValue> tasks,
      final int value) {
    tasks.add(BytesValue.of(value));
    queue.add(BytesValue.of(value));
  }

  private File[] getCurrentFiles(final Path dataDir) {
    return dataDir
        .toFile()
        .listFiles((dir, name) -> name.startsWith(FlatFileTaskCollection.FILENAME_PREFIX));
  }
}
