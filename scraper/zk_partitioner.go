package main

import (
	"encoding/binary"
	"fmt"
	"strings"
	"time"

	"github.com/samuel/go-zookeeper/zk"
)

const zkPrefix = "/scraper/zkPartitioner"

// ZKPartitioner can save and retrieve the last processed offset for a partition
// and topic.
type ZKPartitioner struct {
	conn     *zk.Conn
	versions map[string]int32
}

// NewZKPartitioner creates a new partitioner pointed at the given ZK servers.
func NewZKPartitioner(servers []string) (*ZKPartitioner, error) {
	conn, _, err := zk.Connect(servers, 30*time.Second)
	if err != nil {
		return nil, err
	}

	err = createFolders(conn)
	if err != nil {
		return nil, err
	}

	return &ZKPartitioner{conn: conn, versions: make(map[string]int32)}, nil
}

// NoCheckPoint is returned if GetLastCheckpoint canot find an entry
const NoCheckpoint = -1

// GetLastCheckpoint returns the last checkpoint for a topic and partition. If there
// is no checkpoint saved yet it returns NoCheckPoint.
func (z *ZKPartitioner) GetLastCheckpoint(topic string, partition int32) (int64, error) {
	key := getKey(topic, partition)
	buf, stat, err := z.conn.Get(key)
	if err == zk.ErrNoNode {
		return NoCheckpoint, nil
	}

	z.versions[key] = stat.Version

	if err != nil {
		return NoCheckpoint, err
	}

	return decodeVarint(buf)
}

// SaveCheckpoint saves the last checkpoint for a topic and partition
func (z *ZKPartitioner) SaveCheckpoint(topic string, partition int32, offset int64) error {
	key := getKey(topic, partition)

	exists, _, err := z.conn.Exists(key)
	if err != nil {
		return fmt.Errorf("Failure checking if key %s exists: %v", key, err)
	}

	if !exists {
		_, err := z.conn.Create(key, encodeVarint(offset), 0, zk.WorldACL(zk.PermRead|zk.PermWrite))
		if err != nil {
			return fmt.Errorf("Create key %s failed: err %v", key, err)
		}
		_, stats, err := z.conn.Exists(key)
		if err != nil {
			return fmt.Errorf("Error retrieving just added key %s? %v", key, err)
		}

		z.versions[key] = stats.Version
		return err
	}

	ver, ok := z.versions[key]
	if !ok {
		return fmt.Errorf("Didn't know last version of key %s!", key)
	}
	stat, err := z.conn.Set(key, encodeVarint(offset), ver)
	if err != nil {
		return err
	}

	z.versions[key] = stat.Version
	return nil
}

func decodeVarint(buf []byte) (int64, error) {
	lastCP, n := binary.Varint(buf)
	if lastCP == 0 && n <= 0 {
		return NoCheckpoint, fmt.Errorf("Failed to decode varint from ZK!")
	}

	return lastCP, nil
}

func encodeVarint(i int64) []byte {
	buf := make([]byte, binary.MaxVarintLen64)
	n := binary.PutVarint(buf, i)
	return buf[0:n]
}

func createFolders(conn *zk.Conn) error {
	pieces := strings.Split(zkPrefix, "/")
	folder := ""

	for _, elem := range pieces {
		if elem == "" {
			continue
		}

		folder = folder + "/" + elem
		_, err := conn.Create(folder, []byte{}, 0, zk.WorldACL(zk.PermAll))
		if err != nil && err != zk.ErrNodeExists {
			return fmt.Errorf("Error creating node %s: %v", folder, err)
		}
	}

	return nil
}
func getKey(topic string, partition int32) string {
	return fmt.Sprintf("%s/%s_%d", zkPrefix, topic, partition)
}
