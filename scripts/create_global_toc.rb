require 'yaml'

class GlobalMenu
  def initialize
    @root ||= {}
    @versions ||= {}
    @keys ||= []
    @keys_kaa = getKeys("kaa",["m"])
    @keys.concat @keys_kaa
    @keys.concat getKeys("kaa/m",[])
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
      key = "/#{key}/"
      @versions[key] ||= {}
      ["text","nav","url"].each do |tag|
        @versions[key][tag] = @root[key][tag]
      end
      @versions[key]["link"] = "[#{@root[key]["text"]}](#{@root[key]["url"]})"
      @versions[key]["version"] = @versions[key]["text"].gsub(/K[Aa]{2} (.*)/,'\1')
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
      puts dirname
      header = YAML.load(loadHeader(md_file))
      if header.has_key?('permalink')
        permalink = header['nav']
        path = permalink.split('/')
        permalink = permalink.gsub(":path","#{key}")
        path.delete_if{|k| k.empty?}
        if path[0] == ":path"
          path[0] = "/#{key}/"
        end
        node = createPath(path)
#         puts permalink
        node['url'] = header['permalink'].gsub(":path","#{dirname}")
        #node['nav'] = permalink
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
