FROM fluent/fluentd:v1.16-1

USER root

# Install Elasticsearch output plugin
RUN gem install fluent-plugin-elasticsearch --no-document --version 5.4.3

USER fluent

COPY fluent.conf /fluentd/etc/fluent.conf
