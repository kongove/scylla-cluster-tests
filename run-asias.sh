
# 1.4 1.3.1 1.3
ver=$1

avocado run grow_cluster_test.py:GrowClusterTest.test_grow_3_to_4 --multiplex data_dir/your_config.yaml --filter-only /run/backends/aws/us_east_1 /run/version/$ver /run/databases/scylla --filter-out /run/backends/libvirt  --show-job-log



