package org.hango.cloud.service.impl;

import org.hango.cloud.meta.Graph;
import org.hango.cloud.util.CommonUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2020/5/19
 */
public class GraphFocalizationDelegation {

	private final Map<String, Node> nodes;

	//initNodes已经包含了app和service类型的节点
	private final List<String> initNodes = new ArrayList<>();
	private final Map<String, List<String>> nodeIndexOfApp = new HashMap<>();
	private final int size;
	private final Graph graph;

	public GraphFocalizationDelegation(Graph graph, List<String> apps, int size) {
		this.graph = graph;
		this.size = size;

		Set<String> appsSet = new HashSet<>(apps);
		nodes = CommonUtil.safely(() -> graph.getElements().getNodes())
			.orElse(Collections.emptyList())
			.stream()
			.map(n -> new Node(n, graph.getElements().getEdges()))
			.peek(n -> {
				if (appsSet.contains(n.app)) {
					initNodes.add(n.id);
				}
				if (isValidApp(n.app)) {
					nodeIndexOfApp.computeIfAbsent(n.app, k -> new ArrayList<>()).add(n.id);
				}
			})
			.collect(Collectors.toMap(n -> n.id, n -> n));

		CommonUtil.safely(() -> graph.getElements().getEdges())
			.orElse(Collections.emptyList())
			.forEach(e -> {
				Map<String, String> data = ((Map<String, Map<String, String>>) e).get("data");
				Node source = nodes.get(data.get("source"));
				Node target = nodes.get(data.get("target"));
				source.next.add(target.id);
				target.prev.add(source.id);
			});

		List<Object> nodes = graph.getElements().getNodes();
		nodes.stream()
			.map(node -> ((Map<String, Map<String, String>>) node).get("data"))
			.map(data -> data.get("id"))
			.sorted()
			.forEach(System.out::println);
		List<Object> edges = graph.getElements().getEdges();
		edges.stream()
			.map(edge -> ((Map<String, Map<String, String>>) edge).get("data"))
			.map(data -> "" + data.get("source") + "\n    " + data.get("target"))
			.sorted()
			.forEach(System.out::println);


	}

	private boolean isValidApp(String app) {
		return !app.matches("(unknown|null)\\.(unknown|null)");
	}

	public static Graph focalize(Graph graph, List<String> apps, int size) {
		if (CommonUtil.safelyGet(() -> graph.getElements().getNodes()) == null) {
			return graph;
		}
		return new GraphFocalizationDelegation(graph, apps, size).focalize();
	}

	private Graph focalize() {
		Set<String> focalizedNodeIds = bfsExpand();
		List<Object> focalizedNodes = focalizedNodeIds.stream()
			.map(id -> nodes.get(id).node)
			.collect(Collectors.toList());
		graph.getElements().setNodes(focalizedNodes);
		if (graph.getElements().getEdges() != null) {
			graph.getElements().setEdges(focalizeEdgesByNodes(focalizedNodeIds));
		}
		return graph;
	}

	private List<Object> focalizeEdgesByNodes(Set<String> nodes) {
		return graph.getElements().getEdges().stream()
			.filter(e -> {
				Map<String, String> data = ((Map<String, Map<String, String>>) e).get("data");
				return nodes.contains(data.get("source")) && nodes.contains(data.get("target"));
			})
			.collect(Collectors.toList());
	}

	private Set<String> bfsExpand() {
		Set<String> result = new HashSet<>(initNodes);
		Set<String> current = new HashSet<>(initNodes);
		for (int i = 0; i < size; i++) {
			current = current.stream()
				.flatMap(nodeId -> {
					Node node = nodes.get(nodeId);
					return expandNodesOfSameApp(Stream.concat(node.prev.stream(), node.next.stream()));
				})
				.filter(node -> !result.contains(node))
				.collect(Collectors.toSet());
			if (current.isEmpty()) {
				break;
			}
			result.addAll(current);
		}
		return result;
	}

	private Stream<String> expandNodesOfSameApp(Stream<String> nodes) {
		return nodes
			.map(this.nodes::get)
			.flatMap(n -> {
				if (isValidApp(n.app)) {
					return nodeIndexOfApp.getOrDefault(n.app, Collections.emptyList()).stream();
				} else {
					return Stream.of(n.id);
				}
			});
	}

	private static class Node {
		private final String id;
		private final String app;
		private final Object node;
		private final Set<String> prev = new HashSet<>();
		private final Set<String> next = new HashSet<>();

		public Node(Object n, List<Object> edges) {
			Map<String, String> data = ((Map<String, Map<String, String>>) n).get("data");
			id = data.get("id");
			app = String.format("%s.%s", data.get("app"), data.get("namespace"));
			node = n;
		}

	}

}
