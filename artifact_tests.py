#!/usr/bin/env python

# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
#
# See LICENSE for more details.
#
# Copyright (c) 2019 ScyllaDB

from sdcm.tester import ClusterTester

"""
scylla-docker
single db node
no loader, run workload from frirst db node
disable monitor

test multiple distros as upgrade test.
gce backends

housekeeping test
private repo test
docker test
"""

class ArtifactTest(ClusterTester):
    """
    Scylla artifact tests, basic installation/setup/restart tests of Scylla artifact.
    """

    def test_after_install(self):
        node = self.db_cluster.nodes[0]
        node.check_node_health()

    def test_after_stop_start(self):
        node = self.db_cluster.nodes[0]
        node.check_node_health()

    def test_after_restart(self):
        node = self.db_cluster.nodes[0]
        node.check_node_health()
