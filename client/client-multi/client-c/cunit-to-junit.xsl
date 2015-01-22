<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- 
The MIT License (MIT)

Copyright (c) 2014 Shawn Liang

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" />
    <xsl:template match="/">
        <testsuites>
            <xsl:for-each select="//CUNIT_RUN_SUITE_SUCCESS">
                <xsl:variable name="suiteName" select="normalize-space(SUITE_NAME/text())" />
                <xsl:variable name="numberOfTests"
                    select="count(CUNIT_RUN_TEST_RECORD/CUNIT_RUN_TEST_SUCCESS)" />
                <xsl:variable name="numberOfFailures"
                    select="count(CUNIT_RUN_TEST_RECORD/CUNIT_RUN_TEST_FAILURE)" />
                <testsuite name="{$suiteName}" tests="{$numberOfTests}"
                    time="0" failures="{$numberOfFailures}" errors="0" skipped="0">

                    <xsl:for-each select="CUNIT_RUN_TEST_RECORD/CUNIT_RUN_TEST_SUCCESS">
                        <xsl:variable name="testname"
                            select="normalize-space(TEST_NAME/text())"></xsl:variable>
                        <testcase classname="{$suiteName}" name="{$testname}"
                            time="0.0">
                        </testcase>
                    </xsl:for-each>

                    <xsl:for-each select="CUNIT_RUN_TEST_RECORD/CUNIT_RUN_TEST_FAILURE">
                        <xsl:variable name="testname"
                            select="normalize-space(TEST_NAME/text())"></xsl:variable>
                        <testcase classname="{$suiteName}" name="{$testname}"
                            time="0.0">
                            <failure>
                                Test failed at line
                                <xsl:value-of select="LINE_NUMBER"></xsl:value-of>
                                in file
                                <xsl:value-of select="FILE_NAME"></xsl:value-of>
                                :
                                <xsl:value-of select="CONDITION"></xsl:value-of>
                            </failure>
                        </testcase>
                    </xsl:for-each>
                </testsuite>
            </xsl:for-each>

            <xsl:variable name="totalTestsCount"
                select="normalize-space(//CUNIT_RUN_SUMMARY_RECORD[2]/TOTAL/text())" />

            <xsl:for-each select="//CUNIT_RUN_SUITE_FAILURE">
                <xsl:variable name="suiteName" select="normalize-space(SUITE_NAME/text())" />
                <xsl:variable name="failureReason"
                    select="normalize-space(FAILURE_REASON/text())" />
                <testsuite name="{$suiteName}" tests="0" time="0"
                    failures="0" errors="0" skipped="{$totalTestsCount}">
                    <system-err>
                        <xsl:value-of select="FAILURE_REASON"></xsl:value-of>
                    </system-err>
                </testsuite>
            </xsl:for-each>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>