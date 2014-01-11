/*
 * Copyright (c) 2014. Jordan Williams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jordanwilliams.heftydb.table.file;

import com.jordanwilliams.heftydb.io.DataFile;
import com.jordanwilliams.heftydb.io.MutableDataFile;
import com.jordanwilliams.heftydb.offheap.ByteMap;
import com.jordanwilliams.heftydb.offheap.Memory;
import com.jordanwilliams.heftydb.record.Key;
import com.jordanwilliams.heftydb.state.Paths;
import com.jordanwilliams.heftydb.util.Sizes;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Index {

    private final long tableId;
    private final DataFile indexFile;
    private final IndexBlock rootIndexBlock;
    private final IndexBlock.Cache cache;

    private Index(long tableId, Paths paths, IndexBlock.Cache cache) throws IOException {
        this.tableId = tableId;
        this.indexFile = MutableDataFile.open(paths.indexPath(tableId));
        this.cache = cache;
        long rootIndexBlockOffset = indexFile.readLong(indexFile.size() - Sizes.LONG_SIZE);
        this.rootIndexBlock = readIndexBlock(rootIndexBlockOffset);
    }

    public long recordBlockOffset(Key key, long maxSnapshotId) throws IOException {
        IndexRecord currentIndexRecord = rootIndexBlock.get(key, maxSnapshotId);

        while (currentIndexRecord != null && !currentIndexRecord.isLeaf()) {
            IndexBlock currentIndexBlock = readIndexBlock(currentIndexRecord.offset());
            currentIndexRecord = currentIndexBlock.get(key, maxSnapshotId);
        }

        return currentIndexRecord == null ? -1 : currentIndexRecord.offset();
    }

    public void close() throws IOException {
        indexFile.close();
        rootIndexBlock.memory().release();
    }

    private IndexBlock readIndexBlock(long blockOffset) throws IOException {
        IndexBlock indexBlock = cache.get(tableId, blockOffset);

        if (indexBlock == null) {
            int indexBlockSize = indexFile.readInt(blockOffset);
            Memory indexMemory = Memory.allocate(indexBlockSize);
            ByteBuffer indexBuffer = indexMemory.directBuffer();
            indexFile.read(indexBuffer, blockOffset + Sizes.INT_SIZE);
            indexBuffer.rewind();
            indexBlock = new IndexBlock(new ByteMap(indexMemory));
            cache.put(tableId, blockOffset, indexBlock);
        }

        return indexBlock;
    }

    public static Index open(long tableId, Paths paths, IndexBlock.Cache cache) throws IOException {
        return new Index(tableId, paths, cache);
    }
}
