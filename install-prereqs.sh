#!/usr/bin/env bash
yum install -y epel-release
yum -y update

# Python dependencies
yum install -y python-devel python-pip
pip install --upgrade pip
yum install -y libvirt-devel  # needed for libvirt-python PIP package

# Needed for PhantomJS
yum install -y freetype-devel libpng-devel bzip2 bitmap-fonts fontconfig

# Needed for Cassandra Python driver
yum install -y gcc

# Install OpenSSH client - needed to ssh to DB servers/ Loaders/ monitors
yum install -y openssh-clients
# Install Git - needed to get current SCT branch
yum install -y git

# Install Docker
yum install -y sudo  # needed for Docker container build
curl -fsSL get.docker.com -o get-docker.sh
sh get-docker.sh
groupadd docker || true
usermod -aG docker $USER || true

# Install gsutil
# crcmod is required for downloading large files by gsutil
sudo yum install gcc python-devel python-setuptools redhat-rpm-config -y
sudo easy_install -U pip
sudo pip uninstall crcmod
sudo pip install -U crcmod

# `gsutil` is included in google-cloud-sdk package
cat << EOF > /tmp/google-cloud-sdk.repo
[google-cloud-sdk]
name=Google Cloud SDK
baseurl=https://packages.cloud.google.com/yum/repos/cloud-sdk-el7-x86_64
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg
       https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOF
sudo cp /tmp/google-cloud-sdk.repo /etc/yum.repos.d/google-cloud-sdk.repo
sudo yum install google-cloud-sdk -y

# Config Gcloud account
# gcloud auth activate-service-account --key-file=credential_key.json
# gcloud config set project my-project

# Make sdcm available in python path due to avocado runner bug
if [ "$1" == "docker" ]; then
    ln -s /sct/sdcm /usr/lib/python2.7/site-packages/sdcm
else
    pip install -r requirements-python.txt
    pre-commit install

    ln -s `pwd`/sdcm $(python -c "import site; print site.getsitepackages()[0]")/sdcm
    echo "========================================================="
    echo "Please run 'aws configure' to configure AWS CLI and then"
    echo "run 'get-qa-ssh-keys.sh' to retrieve AWS QA private keys"
    echo "========================================================="
fi
