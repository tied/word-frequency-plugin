package com.frost.plugin.service;

import java.util.Map;

public interface WordFrequencyService {

    /**
     * This method parses all words of all issues (their descriptions and topics)
     * and returns the collection with frequency of each word.
     *
     * @return Map<String, Long>, there String is the word, Long is the frequency number of this word
     */
    Map<String, Long> getFrequency();
}
