#!/bin/bash
set -e

(cd service-layer && ./build_clean.sh)
(cd admin-interface && ./build_clean.sh)
(cd scraper && ./build_clean.sh)
