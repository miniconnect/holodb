FROM dwdraju/alpine-curl-jq:latest AS builder
RUN curl --get \
  --data-urlencode 'query=SELECT ?lemma WHERE \
    { ?lexemeId dct:language wd:Q1860; wikibase:lemma ?lemma. ?lexemeId wikibase:lexicalCategory wd:Q9788 } \
    ORDER BY ?lemma' \
  'https://query.wikidata.org/bigdata/namespace/wdq/sparql' \
  -H 'Accept: application/json' \
  | jq -r '.results.bindings[].lemma.value' \
  > en-letters.txt

FROM miniconnect/holodb:latest
COPY config.yaml /app/config.yaml
COPY --from=builder /en-letters.txt /app/resources/en-letters.txt
