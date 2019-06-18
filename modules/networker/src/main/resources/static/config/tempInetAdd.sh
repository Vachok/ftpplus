#!/bin/sh

sudo echo "169.254.196.176 #1560869175555" >> /etc/pf/24hrs
sudo /etc/initpf.fw
sudo squid -k reconfigure && exit