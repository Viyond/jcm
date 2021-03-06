/*******************************************************************************
 *  Copyright Kevin Lynx (kevinlynx@gmail.com) 2015
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package com.codemacro.jcm.storage;

import java.util.Arrays;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZookeeperPathWatcher {
  private static Logger logger = LoggerFactory.getLogger(ZookeeperPathWatcher.class);
  protected String fullPath;
  protected ZookeeperStorageEngine zkStorage;
  
  abstract String getPath();
  abstract void onListChanged();
  void onChildData(String childName) {}
  void onConnected() {}
  void onSessionExpired() {}
  
  void onRegister(ZookeeperStorageEngine zkStorage, String fullPath) {
    this.zkStorage = zkStorage;
    this.fullPath = fullPath;
  }

  protected boolean writeData(String path, byte[] data) {
    return writeData(path, data, true);
  }

  protected boolean writeData(String path, byte[] data, boolean persistent) {
    ZooKeeper zk = zkStorage.getZooKeeper();
    try {
      if (null == zk.exists(path, false)) {
        zk.create(path, data, Ids.OPEN_ACL_UNSAFE, persistent ? CreateMode.PERSISTENT :
          CreateMode.EPHEMERAL);
      } else {
        zk.setData(path, data, -1);
      }
    } catch (Exception e) {
      logger.error("write data failed on {}", path);
      return false;
    }
    return true;
  }

  protected byte[] getData(String path) {
    try {
      return zkStorage.getZooKeeper().getData(path, true, null);
    } catch (Exception e) {
      logger.error("get data failed on {} {}", path, e);
      return new byte[] {};
    }
  }
  
  protected List<String> getChildren() {
    try {
      return zkStorage.getZooKeeper().getChildren(fullPath, true);
    } catch (Exception e) {
      logger.error("get child list failed on {} {}", fullPath, e);
      return Arrays.asList();
    }
  }
  
  protected void touch(String path) {
    ZooKeeper zk = zkStorage.getZooKeeper();
    final String SEP = "/";
    String curPath = "";
    for (String sub : path.substring(1).split(SEP)) {
      curPath += SEP + sub;
      try {
        if (null == zk.exists(curPath, false)) {
          zk.create(curPath, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
