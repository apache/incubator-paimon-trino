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

import org.apache.paimon.table.Table;
import org.apache.paimon.table.source.ReadBuilder;
import org.apache.paimon.types.RowType;

import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ConnectorPageSource;
import io.trino.spi.connector.ConnectorPageSourceProvider;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorSplit;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.DynamicFilter;
import io.trino.spi.predicate.TupleDomain;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.paimon.trino.ClassLoaderUtils.runWithContextClassLoader;

/** Trino {@link ConnectorPageSourceProvider}. */
public class TrinoPageSourceProvider implements ConnectorPageSourceProvider {

    @Override
    public ConnectorPageSource createPageSource(
            ConnectorTransactionHandle transaction,
            ConnectorSession session,
            ConnectorSplit split,
            ConnectorTableHandle tableHandle,
            List<ColumnHandle> columns,
            DynamicFilter dynamicFilter) {
        TrinoTableHandle trinoTableHandle = (TrinoTableHandle) tableHandle;
        Table table = trinoTableHandle.tableWithDynamicOptions(session);
        return runWithContextClassLoader(
                () ->
                        createPageSource(
                                table, trinoTableHandle.getFilter(), (TrinoSplit) split, columns),
                TrinoPageSourceProvider.class.getClassLoader());
    }

    private ConnectorPageSource createPageSource(
            Table table,
            TupleDomain<TrinoColumnHandle> filter,
            TrinoSplit split,
            List<ColumnHandle> columns) {
        ReadBuilder read = table.newReadBuilder();
        RowType rowType = table.rowType();
        List<String> fieldNames = FieldNameUtils.fieldNames(rowType);
        List<String> projectedFields =
                columns.stream()
                        .map(TrinoColumnHandle.class::cast)
                        .map(TrinoColumnHandle::getColumnName)
                        .collect(Collectors.toList());
        if (!fieldNames.equals(projectedFields)) {
            int[] projected = projectedFields.stream().mapToInt(fieldNames::indexOf).toArray();
            read.withProjection(projected);
        }

        new TrinoFilterConverter(rowType).convert(filter).ifPresent(read::withFilter);

        try {
            return new TrinoPageSource(read.newRead().createReader(split.decodeSplit()), columns);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
