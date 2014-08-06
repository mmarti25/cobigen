/*
 * Copyright © Capgemini 2013. All rights reserved.
 */
package com.capgemini.cobigen.javaplugin.matcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capgemini.cobigen.exceptions.InvalidConfigurationException;
import com.capgemini.cobigen.extension.IMatcher;
import com.capgemini.cobigen.extension.to.MatcherTo;
import com.capgemini.cobigen.extension.to.VariableAssignmentTo;
import com.capgemini.cobigen.javaplugin.inputreader.to.PackageFolder;
import com.capgemini.cobigen.javaplugin.resover.TriggerExpressionResolver;
import com.google.common.collect.Maps;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * Matcher implementation for the Java Plugin
 * @author mbrunnli (08.04.2014)
 */
public class JavaMatcher implements IMatcher {

    /**
     * Assigning logger to JavaClassMatcher
     */
    private static final Logger LOG = LoggerFactory.getLogger(JavaMatcher.class);

    /**
     * Currently supported matcher types
     * @author mbrunnli (08.04.2014)
     */
    private enum MatcherType {
        /** Full Qualified Name Matching */
        FQN,
        /** Package Name Matching */
        PACKAGE,
        /** Expression interpretation */
        EXPRESSION
    }

    /**
     * Available variable types for the matcher
     * @author mbrunnli (08.04.2014)
     */
    private enum VariableType {
        /** Constant variable assignment */
        CONSTANT,
        /** Regular expression group assignment */
        REGEX
    }

    /**
     * {@inheritDoc}
     * @author mbrunnli (08.04.2014)
     */
    @Override
    public boolean matches(MatcherTo matcher) {
        try {
            MatcherType matcherType = Enum.valueOf(MatcherType.class, matcher.getType().toUpperCase());
            switch (matcherType) {
            case FQN:
                String fqn = null;
                if (matcher.getTarget() instanceof Class<?>) {
                    fqn = ((Class<?>) matcher.getTarget()).getCanonicalName();
                } else if (matcher.getTarget() instanceof JavaClass) {
                    fqn = ((JavaClass) matcher.getTarget()).getCanonicalName();
                }
                return fqn != null && fqn.matches(matcher.getValue());
            case PACKAGE:
                return matcher.getTarget() instanceof PackageFolder
                    && ((PackageFolder) matcher.getTarget()).getPackageName().matches(matcher.getValue());
            case EXPRESSION:
                if (matcher.getTarget() instanceof Class<?>) {
                    TriggerExpressionResolver resolver =
                        new TriggerExpressionResolver((Class<?>) matcher.getTarget());
                    return resolver.evaluateExpression(matcher.getValue());
                }
            }
        } catch (IllegalArgumentException e) {
            LOG.info("Matcher type '{}' not registered --> no match!", matcher.getType());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * @throws InvalidConfigurationException
     * @author mbrunnli (08.04.2014)
     */
    @Override
    public Map<String, String> resolveVariables(MatcherTo matcher,
        List<VariableAssignmentTo> variableAssignments) throws InvalidConfigurationException {
        try {
            MatcherType matcherType = Enum.valueOf(MatcherType.class, matcher.getType().toUpperCase());
            switch (matcherType) {
            case FQN:
                String fqn = null;
                if (matcher.getTarget() instanceof Class<?>) {
                    fqn = ((Class<?>) matcher.getTarget()).getCanonicalName();
                } else if (matcher.getTarget() instanceof JavaClass) {
                    fqn = ((JavaClass) matcher.getTarget()).getCanonicalName();
                }
                return getResolvedVariables(matcherType, matcher.getValue(), fqn, variableAssignments);
            default:
                break;
            }
        } catch (IllegalArgumentException e) {
            LOG.warn("Matcher type '{}' not registered --> no match!", matcher.getType());
        }
        return Maps.newHashMap();
    }

    /**
     * Resolves all variables for this trigger
     * @param matcherType
     *            matcher type
     * @param matcherValue
     *            matcher value
     * @param stringToMatch
     *            String to match
     * @param variableAssignments
     *            variable assigments to be resolved
     * @return a {@link Map} from variable name to the resolved value
     * @throws InvalidConfigurationException
     *             if some of the matcher type and variable type combinations are not supported
     * @author mbrunnli (15.04.2013)
     */
    public Map<String, String> getResolvedVariables(MatcherType matcherType, String matcherValue,
        String stringToMatch, List<VariableAssignmentTo> variableAssignments)
        throws InvalidConfigurationException {
        Map<String, String> resolvedVariables = new HashMap<String, String>();
        for (VariableAssignmentTo va : variableAssignments) {
            VariableType variableType = Enum.valueOf(VariableType.class, va.getType().toUpperCase());
            switch (variableType) {
            case CONSTANT:
                resolvedVariables.put(va.getVarName(), va.getValue());
                break;
            case REGEX:
                resolvedVariables.put(va.getVarName(),
                    resolveRegexValue(matcherType, matcherValue, stringToMatch, va));
                break;
            }
        }
        return resolvedVariables;
    }

    /**
     * Resolves the variable assignments of type {@link VariableType#REGEX}
     * @param matcherType
     *            type of the matcher
     * @param matcherValue
     *            value of the matcher
     * @param stringToMatch
     *            string to match
     * @param va
     *            {@link VariableAssignmentTo} to be resolved
     * @return the resolved variable
     * @throws InvalidConfigurationException
     *             thrown if the matcher type and matcher value does not work in combination
     * @author mbrunnli (08.04.2014)
     */
    private String resolveRegexValue(MatcherType matcherType, String matcherValue, String stringToMatch,
        VariableAssignmentTo va) throws InvalidConfigurationException {
        Pattern p = Pattern.compile(matcherValue);
        Matcher m = p.matcher(stringToMatch);

        if (m != null) {
            if (m.matches()) {
                try {
                    String value = m.group(Integer.parseInt(va.getValue()));
                    if (value == null)
                        throw new InvalidConfigurationException(
                            "The VariableAssignment '"
                                + va.getType().toUpperCase()
                                + "' in the Matcher of type '"
                                + matcherType.toString()
                                + "' does not match a regular expression group of the matcher value.\nCurrent value: '"
                                + va.getValue() + "'");
                    return value;
                } catch (NumberFormatException e) {
                    LOG.error(
                        "The VariableAssignment '{}' in the Matcher of type '{}' should have an integer as value representing a regular expression group.\nCurrent value: '{}'",
                        va.getType().toUpperCase(), matcherType.toString(), va.getValue(), e);
                    throw new InvalidConfigurationException(
                        "The VariableAssignment '"
                            + va.getType().toUpperCase()
                            + "' in the Matcher of type '"
                            + matcherType.toString()
                            + "' should have an integer as value representing a regular expression group.\nCurrent value: '"
                            + va.getValue() + "'");
                }
            } // else should not occur as #matches(...) will be called beforehand
        } else {
            throw new InvalidConfigurationException(
                "The VariableAssignment type 'REGEX' can only be combined with matcher type 'FQN' or 'PACKAGE'");
        }
        return null; // should not occur
    }

}