FROM dwdraju/alpine-curl-jq:latest AS builder
COPY professionals.sparql /professionals.sparql
RUN \
  curl --get \
    --data-urlencode "query=$( cat '/professionals.sparql' )" \
    -H 'Accept: application/json' \
    'https://query.wikidata.org/bigdata/namespace/wdq/sparql' \
  | jq -r '.results.bindings[].professionLabel.value' \
  | sort -u \
  > /professionals.txt

FROM miniconnect/holodb:latest
COPY config.yaml /app/config.yaml
COPY --from=builder /professionals.txt /app/resources/professionals.txt
