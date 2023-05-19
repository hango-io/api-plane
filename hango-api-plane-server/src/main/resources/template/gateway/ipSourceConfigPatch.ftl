applyTo: NETWORK_FILTER
match:
  context: GATEWAY
  listener:
    filterChain:
      filter:
        name: envoy.filters.network.http_connection_manager
    portNumber: ${t_envoy_filter_port_number}
patch:
  operation: MERGE
  value:
    typed_config:
      "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
      use_remote_address: ${t_ip_config_patch_use_remote_address}
<#if t_ip_config_patch_xff_num_trusted_hops?has_content>
      xff_num_trusted_hops: ${t_ip_config_patch_xff_num_trusted_hops}
</#if>
<#if t_ip_config_patch_custom_header?has_content>
      original_ip_detection_extensions:
      - name: envoy.http.original_ip_detection.custom_header
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.http.original_ip_detection.custom_header.v3.CustomHeaderConfig
          header_name: ${t_ip_config_patch_custom_header}
</#if>