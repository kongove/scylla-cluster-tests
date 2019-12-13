#!groovy

def call() {
    pipeline {
        agent {
            label {
                label getJenkinsLabels(params.backend, params.aws_region)
            }
        }

        environment {
            AWS_ACCESS_KEY_ID     = credentials('qa-aws-secret-key-id')
            AWS_SECRET_ACCESS_KEY = credentials('qa-aws-secret-access-key')
		}

         parameters {

            string(defaultValue: 'git@github.com:scylladb/scylla-cluster-tests.git',
                   description: 'sct git repo',
                   name: 'sct_repo')

            string(defaultValue: 'master',
                   description: 'sct git branch',
                   name: 'sct_branch')

            string(defaultValue: "upgrade_test.UpgradeTest.test_rolling_upgrade",
                   description: '',
                   name: 'test_name')
            string(defaultValue: "test-cases/upgrades/rolling-upgrade.yaml",
                   description: '',
                   name: 'test_config')
            string(defaultValue: "3.1",
                   description: '',
                   name: 'base_version')
            string(defaultValue: "centos",
                   description: '',
                   name: 'linux_distro')
            string(defaultValue: "https://www.googleapis.com/compute/v1/projects/centos-cloud/global/images/family/centos-7",
                   description: '',
                   name: 'gce_image_db')
            string(defaultValue: "gce",
               description: 'aws|gce',
               name: 'backend')
            string(defaultValue: "eu-west-1",
               description: 'us-east-1|eu-west-1',
               name: 'aws_region')
            //string(defaultValue: '', description: '', name: 'scylla_ami_id')
            //string(defaultValue: '', description: '', name: 'scylla_version')
            string(defaultValue: '', description: '', name: 'new_scylla_repo')
            //string(defaultValue: "spot_low_price",
            //       description: 'spot_low_price|on_demand|spot_fleet|spot_low_price|spot_duration',
            //       name: 'provision_type')

            string(defaultValue: "keep-on-failure",
                   description: 'keep|keep-on-failure|destroy',
                   name: 'post_behavior_db_nodes')
            string(defaultValue: "destroy",
                   description: 'keep|keep-on-failure|destroy',
                   name: 'post_behavior_loader_nodes')
            string(defaultValue: "keep-on-failure",
                   description: 'keep|keep-on-failure|destroy',
                   name: 'post_behavior_monitor_nodes')

            string(defaultValue: '360',
                   description: 'timeout for jenkins job in minutes',
                   name: 'timeout')

            string(defaultValue: '',
                   description: 'cloud path for RPMs, s3:// or gs://',
                   name: 'update_db_packages')

            booleanParam(defaultValue: true,
                 description: 'Workaround a known kernel bug which causes iotune to fail in scylla_io_setup, only effect GCE backend',
                 name: 'workaround_kernel_bug_for_iotune')
        }
        options {
            timestamps()
            disableConcurrentBuilds()
            timeout([time: params.timeout, unit: "MINUTES"])
            buildDiscarder(logRotator(numToKeepStr: '20'))
        }
        stages {
            stage('Checkout') {
               steps {
                  dir('scylla-cluster-tests') {
                      git(url: params.sct_repo,
                            credentialsId:'b8a774da-0e46-4c91-9f74-09caebaea261',
                            branch: params.sct_branch)

                      dir("scylla-qa-internal") {
                        git(url: 'git@github.com:scylladb/scylla-qa-internal.git',
                            credentialsId:'b8a774da-0e46-4c91-9f74-09caebaea261',
                            branch: 'master')
                      }
                  }
               }
            }
            stage('Run SCT Test') {
                steps {
                    script {
                        wrap([$class: 'BuildUser']) {
                            dir('scylla-cluster-tests') {

                                // handle params which can be a json list
                                def aws_region = groovy.json.JsonOutput.toJson(params.aws_region)
                                def test_config = groovy.json.JsonOutput.toJson(params.test_config)

                                sh """
                                #!/bin/bash
                                set -xe
                                env

                                export SCT_CLUSTER_BACKEND="${params.backend}"
                                #export SCT_REGION_NAME=${aws_region}
                                export SCT_CONFIG_FILES="${test_config}"

                                export SCT_SCYLLA_VERSION=${base_version}

                                if [[ ! -z "${params.scylla_ami_id}" ]] ; then
                                    export SCT_AMI_ID_DB_SCYLLA="${params.scylla_ami_id}"
                                elif [[ ! -z "${params.scylla_version}" ]] ; then
                                    export SCT_SCYLLA_VERSION="${params.scylla_version}"
                                elif [[ ! -z "${params.new_scylla_repo}" ]] ; then
                                    export SCT_NEW_SCYLLA_REPO="${params.new_scylla_repo}"
                                else
                                    echo "need to choose one of SCT_AMI_ID_DB_SCYLLA | SCT_SCYLLA_VERSION | SCT_SCYLLA_REPO"
                                    exit 1
                                fi
                                if [[ ! -z "${params.update_db_packages}" ]]; then
                                    export SCT_UPDATE_DB_PACKAGES="${params.update_db_packages}"
                                fi
                                export SCT_POST_BEHAVIOR_DB_NODES="${params.post_behavior_db_nodes}"
                                export SCT_POST_BEHAVIOR_LOADER_NODES="${params.post_behavior_loader_nodes}"
                                export SCT_POST_BEHAVIOR_MONITOR_NODES="${params.post_behavior_monitor_nodes}"
                                #export SCT_INSTANCE_PROVISION="${params.provision_type}"
                                #export SCT_AMI_ID_DB_SCYLLA_DESC=\$(echo \$GIT_BRANCH | sed -E 's+(origin/|origin/branch-)++')
                                #export SCT_AMI_ID_DB_SCYLLA_DESC=\$(echo \$SCT_AMI_ID_DB_SCYLLA_DESC | tr ._ - | cut -c1-8 )

                                export SCT_GCE_IMAGE_DB=${pipelineParams.gce_image_db}

                                export SCT_WORKAROUND_KERNEL_BUG_FOR_IOTUNE=${params.workaround_kernel_bug_for_iotune}
                                export SCT_COLLECT_LOGS=true
                                export SCT_EXECUTE_POST_BEHAVIOR=true

                                echo "start test ......."
                                ./docker/env/hydra.sh run-test ${params.test_name} --backend ${params.backend}  --logdir /sct
                                echo "end test ....."
                                """
                            }
                        }
                    }
                }
            }
        }
    }

}
