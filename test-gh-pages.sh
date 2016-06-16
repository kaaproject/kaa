#!/bin/bash
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

versions=`git tag`
curr_tag=`git tag --contains`
curr_branch=$(git rev-parse --symbolic-full-name --abbrev-ref HEAD)
gh_pages=gh-pages
latest='v0.7.0'
git_root=$(pwd)

# if current tag is empty we should use alias current instead of tag name
if [ x$cur_tag == x ]; then
  curr_tag="current"
fi

if [[ -d doc ]]; then
  echo "Local deploing gh-pages for $curr_tag tag"
  jekyll_root=test-gh-pages-$curr_tag
  if [ ! -d $jekyll_root ]; then
    git clone .git --branch gh-pages-stub $jekyll_root --single-branch
    mkdir -p $jekyll_root/kaa
    mkdir -p $jekyll_root/kaa/m
    mkdir -p $jekyll_root/_data
    ln -s $PWD/doc $PWD/$jekyll_root/kaa/$curr_tag
    ln -s $PWD/doc $PWD/$jekyll_root/kaa/latest
    submodule_cmd=$(printf 'echo $(basename $name) $(pwd) %s' $git_root)
    git submodule foreach "$submodule_cmd"
    
    submodule_cmd=$(printf '[ -d doc ] \
    && ln -sfn $(pwd)/doc %s/%s/%s/$(basename $name) && echo "Created submodule doc symlink for $(basename $name)" \
    || echo "Missing doc folder for $(basename $name)"' \
    $git_root $jekyll_root 'kaa/m')
    #echo "$submodule_cmd"
    git submodule foreach "$submodule_cmd"
  fi
  cd $jekyll_root
  ruby scripts/create_global_toc.rb
  jekyll serve
else
  echo "Nothing to do"
fi
