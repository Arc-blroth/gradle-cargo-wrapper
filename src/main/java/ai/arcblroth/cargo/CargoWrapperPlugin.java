// Copyright 2021 Arc'blroth
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ai.arcblroth.cargo;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * A plugin that wraps Rust's Cargo build system,
 * for embedding Rust libraries in Java projects.
 */
@SuppressWarnings("unused")
public class CargoWrapperPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getConfigurations().create("default");
        CargoExtension extension = project.getExtensions().create("cargo", CargoExtension.class);
        CargoTask buildTask = project.getTasks().register("build", CargoTask.class).get();
        project.getArtifacts().add("default", buildTask);
        project.afterEvaluate(project2 -> buildTask.configure(extension));
    }
}
