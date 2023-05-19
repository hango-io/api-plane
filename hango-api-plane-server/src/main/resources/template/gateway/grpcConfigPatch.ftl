applyTo: HTTP_FILTER
match:
  context: GATEWAY
  listener:
    filterChain:
      filter:
        name: envoy.filters.network.http_connection_manager
        subFilter:
          name: envoy.filters.http.router
    portNumber: ${t_envoy_filter_port_number}
patch:
  operation: INSERT_BEFORE
  value:
    name: envoy.filters.http.grpc_json_transcoder
    typed_config:
      "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_json_transcoder.v3.GrpcJsonTranscoder
      match_incoming_request_route: true
      proto_descriptor_bin: ${t_grpc_config_patch_proto_descriptor_bin}
      services: ${t_grpc_config_patch_services}
      print_options:
        add_whitespace: true
        always_print_primitive_fields: true
        always_print_enums_as_ints: true
        preserve_proto_field_names: false