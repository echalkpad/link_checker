package main

import "testing"

func TestBadConfig(t *testing.T) {
	config, err := ConfigFromFile("test_Assets/bad_config.yml")
	if config != nil || err == nil {
		t.Errorf("Expected config to be nil (was %v) and err to not be nil (was %v)", config, err)
	}
}

func TestGoodConfig(t *testing.T) {
	expectedDataAPI := "http://localhost:8080"
	expectedKafka := "localhost:9200"
	expectedZookeeper := "localhost:2181"

	config, err := ConfigFromFile("test_assets/good_config.yml")
	if err != nil {
		t.Errorf("Failed to load config: %v", err)
		return
	}

	if config.DataAPIAddr != expectedDataAPI {
		t.Errorf("Expected data api to be %s, was %s", expectedDataAPI, config.DataAPIAddr)
	}

	if config.KafkaAddr != expectedKafka {
		t.Errorf("Expected kafka address to be %s was %s", expectedKafka, config.KafkaAddr)
	}

	if config.ZookeeperAddr != expectedZookeeper {
		t.Errorf("Expected zookeeper address to be %s was %s", expectedZookeeper, config.ZookeeperAddr)
	}
}
