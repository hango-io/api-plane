package org.hango.cloud.meta;

import java.util.ArrayList;
import java.util.List;


public class Graph {

    public Graph() {
        this.elements = new Elements();
    }

    private Long timestamp;

    private Long duration;

    private String graphType;

    private Elements elements;

    public class Elements {

        public Elements() {
            this.nodes = new ArrayList<>();
            this.edges = new ArrayList<>();
        }

        private List<Object> nodes;
        private List<Object> edges;

        public List<Object> getNodes() {
            return nodes;
        }

        public void setNodes(List<Object> nodes) {
            this.nodes = nodes;
        }

        public List<Object> getEdges() {
            return edges;
        }

        public void setEdges(List<Object> edges) {
            this.edges = edges;
        }
    }


    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getGraphType() {
        return graphType;
    }

    public void setGraphType(String graphType) {
        this.graphType = graphType;
    }

    public Elements getElements() {
        return elements;
    }

    public void setElements(Elements elements) {
        this.elements = elements;
    }
}
