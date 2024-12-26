echo 'Cloning stuffs to build for peridot'

git clone https://github.com/Soverzion-Peridot/vendor_xiaomi_peridot -b 14 vendor/xiaomi/peridot
git clone https://github.com/Soverzion-Peridot/device_xiaomi_peridot-kernel -b 14 device/xiaomi/peridot-kernel

git clone https://github.com/Soverzion-Peridot/device_xiaomi_peridot-miuicamera device/xiaomi/peridot-miuicamera
git clone https://gitlab.com/Soverzion-HQ/vendor_xiaomi_peridot-miuicamera vendor/xiaomi/peridot-miuicamera

rm -rf hardware/xiaomi
git clone https://github.com/Soverzion-Peridot/hardware_xiaomi -b lineage-21 hardware/xiaomi
git clone https://github.com/Soverzion-Peridot/vendor_lineage-priv vendor/lineage-priv

echo 'Cloning completed. Time to lunch. Miaw'
