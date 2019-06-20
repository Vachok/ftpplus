#!/bin/sh

sudo grep -v '.www.velkomfood.ru' /etc/pf/allowdomain > /etc/pf/allowdomain_tmp
sudo grep -v '77.244.214.54 #.www.velkomfood.ru' /etc/pf/allowip > /etc/pf/allowip_tmp
sudo cp /etc/pf/allowdomain_tmp /etc/pf/allowdomain
sudo cp /etc/pf/allowip_tmp /etc/pf/allowip
sudo echo ".www.velkomfood.ru" >> /etc/pf/allowdomain
sudo echo "77.244.214.54 #.www.velkomfood.ru" >> /etc/pf/allowip
sudo tail /etc/pf/allowdomain
sudo tail /etc/pf/allowip
sudo squid && sudo squid -k reconfigure
sudo /etc/initpf.fw && exit