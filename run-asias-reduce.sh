
# 1.4 1.3.1 1.3
ver=$1

avocado run reduce_cluster_test.py:ReduceClusterTest.test_reduce_4_to_3 --multiplex data_dir/your_config.yaml --filter-only /run/backends/aws/us_east_1 /run/version/$ver /run/databases/scylla --filter-out /run/backends/libvirt  --show-job-log



