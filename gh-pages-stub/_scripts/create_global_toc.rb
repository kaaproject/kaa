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

DOCS_ROOT = YAML.load_file("_data/generated_config.yml")["docs_root"]

if not (Array.new).methods.include?(:to_h)
  class Array
    def to_h
      hash = Hash.new
      self.each do |element|
        array_pair = element.to_a
        hash[array_pair[0]] = array_pair[1]
      end
      return hash
    end
  end
end

class GlobalMenu
  def initialize
    @root ||= {}
    @versions ||= {}
    @keys ||= []
    @keys_kaa = getKeys("#{DOCS_ROOT}",["m","latest"])
    @keys.concat @keys_kaa
    @keys.concat getKeys("#{DOCS_ROOT}/m",["latest"])
  end

  ##
  # Process
  ##
  def process()
    @keys.each do |key|
      loadDoc(key)
    end
    @root = sortSubitems(@root)
    File.open("_data/menu.yml", 'w') { |f| YAML.dump(@root, f) }
    @keys_kaa.each do |key|
      begin
        key = "/#{key}/"
        @versions[key] ||= {}
        ["text","url"].each do |tag|
          @versions[key][tag] = @root[key][tag]
        end
        # According to new structure
        @versions[key]["text"] = (@versions[key]["url"].split("/"))[2]
        @versions[key]["link"] = "[#{@root[key]["text"]}](#{@root[key]["url"]})"
        @versions[key]["version"] = @versions[key]["text"].gsub(/K[Aa]{2} (.*)/,'\1')
      rescue => e
        puts "caught exception #{e}! for key : #{key}."
      end
    end
    File.open("_data/versions.yml", 'w') { |f| YAML.dump(@versions, f) }
#     puts @root.to_yaml
  end

  ##
  # Get Keys from directories
  ##
  def getKeys(path,exept)
    keys ||= []
    Dir.glob("#{path}/*") do |doc_dir|
      if File.directory?(doc_dir)
        doc_dir = File.basename(doc_dir)
        keys << "#{path}/#{doc_dir}" unless exept.include?(doc_dir)
      end
    end
    return keys
  end

  ##
  # Sort menu tree by titles and sort_idx
  ##
  def sortSubitems(node)
    node=node.sort.to_h
    node.delete_if{|k, v| v['_sort_idx'].nil?}
    node = node.sort_by{|k, v| v['_sort_idx']}.to_h
    node.each do |key, subitems|
      node[key] = node[key].sort.to_h
      node[key]['subitems'] = sortSubitems(node[key]['subitems'])
    end
    return node
  end

  ##
  # Load all markdown files and parce yaml headers to extract nav information
  ##
  def loadDoc(key)
    Dir.glob("#{key}/**/index.md") do |md_file|
      dirname = File.dirname(md_file)
      header = YAML.load(loadHeader(md_file))
      if header.has_key?('permalink')
        url = md_file.gsub("/index.md","")
        url = url.gsub("#{key}","")
        #url = url.gsub("//","/")
        path = url.split('/')
        path[0] = "/#{key}/"
        path.delete_if{|k| k.empty?}
        #puts "path", path
        node = createPath(path)
        node['url'] = header['permalink'].gsub(":path","#{dirname}")
        if header.has_key?('sort_idx')
          node['_sort_idx'] = header['sort_idx']
        else
          node['_sort_idx'] = 1000
        end
        if header.has_key?("title")
          if node['level'] == 1 and key == 'kaa/latest'
            node['text']= "Kaa latest"
          else
            node['text']=header['title']
          end
        end
      end
    end
  end

  ##
  # Get node for md file by nav path
  ##
  def getNode(path)
    subitems = @root
    node={}
    path.each do |k|
      node['level'] = deep
      if !subitems.has_key?(k)
        return false,{}
      end
      node = subitems[k]
      deep += 1
      subitems = node['subitems']
    end
    return true,node
  end

  ##
  # Create nav path
  ##
  def createPath(path)
    subitems = @root
    node={}
    deep = 0
    path.each do |k|
      node['level'] = deep
      if !subitems.has_key?(k)
        subitems[k] = {}
        subitems[k]['subitems']={}
      end
      node = subitems[k]
      deep += 1
      subitems = node['subitems']
    end
    return node
  end

  ##
  # Load YAML header
  ##
  def loadHeader(file)
    is_header_mode = 0
    header = ""
    #puts "working on: #{file}..."
    fileObj = File.new(file, "r")
    while (line = fileObj.gets)
      if line.match("---\n")
        is_header_mode += 1
      end
      if is_header_mode > 0
        header << line
      end
      if is_header_mode >1
        break
      end
    end
    fileObj.close
    return header
  end
end

gm = GlobalMenu.new
gm.process()
