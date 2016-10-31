##############################################################################
## Copyright 2014-2016 CyberVision, Inc.
## <p/>
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
## <p/>
## http://www.apache.org/licenses/LICENSE-2.0
## <p/>
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##############################################################################
require 'yaml'
require "fileutils"

CONFIG_DATA = YAML.load_file("_data/generated_config.yml")
DOCS_ROOT = CONFIG_DATA["docs_root"]
LATEST_VERSION = CONFIG_DATA["version"]
LATEST_DIR = "#{DOCS_ROOT}/latest"
REDIRECT_CONTENT = <<-eos
---
layout: redirected
sitemap: false
redirect_to: $LATEST
---
eos

FileUtils.rm_rf(LATEST_DIR)
FileUtils.cp_r("#{DOCS_ROOT}/#{LATEST_VERSION}/", LATEST_DIR)
Dir.glob("#{LATEST_DIR}/**/index.md").each { |file_name|
	File.truncate(file_name,0)
	file = File.new(file_name,"w")
	file.write(REDIRECT_CONTENT)
}
