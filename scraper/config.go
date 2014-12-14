package main

import (
	"fmt"
	"io/ioutil"

	"gopkg.in/yaml.v2"
)

// Config holds the relevant config info for the app
type Config struct {
	ZookeeperAddr string `yaml:"zookeeper"`
	KafkaAddr     string `yaml:"kafka"`
	DataAPIAddr   string `yaml:"data_api"`
}

// ConfigFromFile unmarshals a config struct from the given filename.
func ConfigFromFile(filename string) (*Config, error) {
	buf, err := ioutil.ReadFile(filename)
	if err != nil {
		return nil, err
	}

	return configFromBuf(buf)
}

func configFromBuf(buf []byte) (*Config, error) {
	var config Config
	err := yaml.Unmarshal(buf, &config)
	if err != nil {
		return nil, err
	}

	err = validateConfig(&config)
	if err != nil {
		return nil, err
	}

	return &config, err
}

func validateConfig(config *Config) error {
	if config.ZookeeperAddr == "" {
		return fmt.Errorf("Zookeeper address must be set")
	}

	if config.KafkaAddr == "" {
		return fmt.Errorf("Kafka address must be set")
	}

	if config.DataAPIAddr == "" {
		return fmt.Errorf("DataApi address must be valiid")
	}

	return nil
}
