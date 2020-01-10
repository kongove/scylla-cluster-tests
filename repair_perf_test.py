#!/usr/bin/env python

import random

from sdcm.tester import ClusterTester


class RepairPerfTest(ClusterTester):

    def test_bootstrap(self):
        stress_cmd = self.params.get('prepare_write_cmd')
        stress = self.run_stress_thread(stress_cmd=stress_cmd)
        self.verify_stress_thread(cs_thread_pool=stress)

        stress_cmd = self.params.get('stress_cmd')
        stress = self.run_stress_thread(stress_cmd=stress_cmd)

        self.log.info("Adding new node to cluster...")
        enable_repair_based_node_ops = self.params.get('enable_repair_based_node_ops')
        new_node = self.cluster.add_nodes(count=1, enable_auto_bootstrap=True,
                                          enable_repair_based_node_ops=enable_repair_based_node_ops)[0]
        self.cluster.wait_for_init(node_list=[new_node], timeout=10800)
        self.cluster.wait_for_nodes_up_and_normal(nodes=[new_node])
        self.monitoring_set.reconfigure_scylla_monitoring()

        self.verify_stress_thread(cs_thread_pool=stress)

    def test_decommission(self):
        stress_cmd = self.params.get('prepare_write_cmd')
        stress = self.run_stress_thread(stress_cmd=stress_cmd)
        self.verify_stress_thread(cs_thread_pool=stress)

        stress_cmd = self.params.get('stress_cmd')
        stress = self.run_stress_thread(stress_cmd=stress_cmd)

        non_seed_nodes = [node for node in self.cluster.nodes if not node.is_seed and not node.running_nemesis]
        target_node = random.choice(non_seed_nodes)

        self.log.info("Decommission the one node from cluster...")
        enable_repair_based_node_ops = self.params.get('enable_repair_based_node_ops')
        target_node.remoter.run("echo 'authorizer: %s' |sudo tee --append /etc/scylla/scylla.yaml" %
                                enable_repair_based_node_ops)

        target_node.run_nodetool("decommission")
        self.cluster.terminate_node(target_node)
        self.monitoring_set.reconfigure_scylla_monitoring()

        self.verify_stress_thread(cs_thread_pool=stress)
