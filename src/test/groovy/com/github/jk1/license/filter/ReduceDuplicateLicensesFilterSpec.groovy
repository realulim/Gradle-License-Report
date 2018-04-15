package com.github.jk1.license.filter

import com.github.jk1.license.ProjectBuilder
import com.github.jk1.license.ProjectData
import spock.lang.Specification

import static com.github.jk1.license.ProjectBuilder.json
import static com.github.jk1.license.ProjectDataFixture.APACHE2_LICENSE

class ReduceDuplicateLicensesFilterSpec extends Specification {
    def duplicateFilter = new ReduceDuplicateLicensesFilter()
    ProjectBuilder builder = new ProjectBuilder()

    def "when two licenses are equal in one pom, they should be unified"() {
        // add two configuration with slightly different name (to avoid HashMap swallow one)
        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(APACHE2_LICENSE(), name: "Apache 2")
                        license(APACHE2_LICENSE())
                    }
                }
            }
        }
        // then make both name equal, what results in a HashSet with two entries with two equal hashes
        projectData.configurations*.dependencies.flatten().poms.flatten().licenses.flatten().each {
            it.name = APACHE2_LICENSE().name
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(APACHE2_LICENSE())
                    }
                }
            }
        }

        when:
        def result = duplicateFilter.filter(projectData)

        then:
        json(result) == json(expected)
    }

    def "when two license names in a pom are equal, they should be unified, even if other details differ"() {
        // add two configuration with slightly different name (to avoid HashMap swallow one)
        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(APACHE2_LICENSE())
                        license(APACHE2_LICENSE(), url: "someting else")
                        license(APACHE2_LICENSE(), distribution: "someting else")
                        license(APACHE2_LICENSE(), comments: "someting else")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    pom("pom1") {
                        license(APACHE2_LICENSE())
                    }
                }
            }
        }

        when:
        def result = duplicateFilter.filter(projectData)

        then:
        json(result) == json(expected)
    }

    def "when two licenses files are equals, they should be unified"() {
        ProjectData projectData = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", licenseUrl: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                        licenseFileDetails(file: "apache2-license.txt", licenseUrl: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
        ProjectData expected = builder.project {
            configuration("runtime") {
                module("mod1") {
                    licenseFiles {
                        licenseFileDetails(file: "apache2-license.txt", licenseUrl: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }

        when:
        def result = duplicateFilter.filter(projectData)

        then:
        json(result) == json(expected)
    }
}