- version 3.1.0
    - support OpenSearch 2.6.0+ in addition to ElasticSearch
    - analysis-sudachi plugin is now can be extended by other plugins. Loading sudachi plugins from extending plugins is supported as well
- version 3.0.0
    - Plugin is now implemented in Kotlin
- version 2.1.0
    - Added a new property `additional_settings` to write Sudachi settings directly in config
    - Added support for specifying Elasticsearch version at build time
- version 2.0.3
    - Fix duplicated tokens for OOVs with `sudachi_split` filter's `extended mode`
- version 2.0.2
    - Upgrade Sudachi to 0.4.3
        - Fix overrun with surrogate pairs
- version 2.0.1
    - Upgrade Sudachi to 0.4.2
        - Fix buffer overrun with character normalization
- version 2.0.0
    - New mode `split_mode` was added
    - New filter `sudachi_split` was added instead of `mode`
    - `mode` was deperecated
    - Upgrade Sudachi morphological analyzer to 0.4.1
    - Words containing periods are no longer split
    - Fix a bug causing wrong offsets with `icu_normalizer`

- version 1.3.2
    - Upgrade Sudachi morphological analyzer to 0.3.1

- version 1.3.1
    - Upgrade Sudachi morphological analyzer to 0.3.0
    - Minor bug fix

- version 1.3.0
    - Upgrade Sudachi morphological analyzer to 0.2.0
    - Import Sudachi from maven central repository
    - Minor bug fix

- version 1.2.0
    - Upgrading Sudachi morphological analyzer to 0.2.0-SNAPSHOT
    - New filter `sudachi_normalizedform` was added; see [sudachi_normalizedform](#sudachi_normalizedform)
    - Default normalization behavior was changed; neather baseform filter and normalziedform filter not applied
    - `sudachi_readingform` filter was changed with new romaji mappings based on MS-IME

- version 1.1.0
    - `part-of-speech forward matching` is available on `stoptags`; see [sudachi_part_of_speech](#sudachi_part_of_speech)

- version 1.0.0
    - first release
