/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.trino;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** {@link TestTrinoITCase} for Trino 422. */
public class TestTrino427ITCase extends TestTrinoITCase {

    public TestTrino427ITCase() {
        super(427);
    }

    @Test
    public void testSchemaEvolution() {
        String s =
                sql(
                        "SELECT boolean, tinyint, smallint, int, bigint,float,double,char,varchar, date,timestamp_0, "
                                + "timestamp_3, timestamp_6, timestamp_tz, decimal, to_hex(varbinary), array, map, row FROM paimon.default.t100");
        System.out.println(s);
        assertThat(
                        sql(
                                "SELECT boolean, tinyint, smallint, int, bigint,float,double,char,varchar, date,timestamp_0, "
                                        + "timestamp_3, timestamp_6, timestamp_tz, decimal, to_hex(varbinary), array, map, row FROM paimon.default.t100"))
                .isEqualTo(
                        "[[true, 1, null, 1, 1, 1.0, 1.0, char1, varchar1, 1970-01-01, 2023-09-12T07:54:48, 2023-09-12T07:54:48.001, 2023-09-12T07:54:48.001001, 2023-09-11T23:54:48.002Z[UTC], 0.10000, 010203, [1, 1, 1], {1=1}, [1, 1]], [true, 1, null, 1, 1, 1.0, 1.0, char1, varchar1, 1970-01-01, 2023-09-12T07:54:48, 2023-09-12T07:54:48.001, 2023-09-12T07:54:48.001001, 2023-09-11T23:54:48.002Z[UTC], 0.10000, 010203, [1, 1, 1], {1=1}, [1, 1]], [true, 1, 1, 1, 1, 1.0, 1.0, char1, varchar1, 1970-01-01, 2023-09-12T07:54:48, 2023-09-12T07:54:48.001, 2023-09-12T07:54:48.001001, 2023-09-11T23:54:48.002Z[UTC], 0.10000, 010203, [1, 1, 1], {1=1}, [1, 1]]]");
    }
}