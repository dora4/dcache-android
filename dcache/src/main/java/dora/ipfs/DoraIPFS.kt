package dora.ipfs

import io.ipfs.api.IPFS
import io.ipfs.api.MerkleNode
import io.ipfs.api.NamedStreamable
import io.ipfs.multihash.Multihash
import java.io.File

class DoraIPFS(val multiAddr: String) {

    val ipfs = IPFS(multiAddr)

    /**
     * 向IPFS网络写数据。
     */
    fun write(file: File) : MerkleNode {
        val file = NamedStreamable.FileWrapper(file)
        val merkleNode = ipfs.add(file)[0]
        return merkleNode
    }

    /**
     * 从IPFS网络读取数据。
     *
     * @param base58编码的hash
     */
    fun read(hash: String) : ByteArray {
        val filePointer = Multihash.fromBase58(hash)
        val bytes = ipfs.cat(filePointer)
        return bytes
    }
}