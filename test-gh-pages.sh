#!/bin/sh
#
# Copyright 2014-2016 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

curr_tag=$(git tag --contains)

# if current tag is empty we should use alias current instead of tag name
if [ x"$curr_tag" = x ]; then
  curr_tag="current"
fi

if [ -d doc ]; then
  echo "Local deploing gh-pages for $curr_tag tag"
  jekyll_root=test-gh-pages-$curr_tag
  latest=$curr_tag
  if [ ! -d $jekyll_root ]; then
    git clone .git --branch gh-pages-stub $jekyll_root --single-branch

    mkdir -p $jekyll_root/kaa
    mkdir -p $jekyll_root/_data
    ln -s "$PWD/doc" "$PWD/$jekyll_root/kaa/$curr_tag"
  fi
  cd $jekyll_root
  echo '---\nversion:' $latest > _data/latest_version.yml
  ruby scripts/create_global_toc.rb
  jekyll serve "$@"
else
  echo "Nothing to do"
fi
