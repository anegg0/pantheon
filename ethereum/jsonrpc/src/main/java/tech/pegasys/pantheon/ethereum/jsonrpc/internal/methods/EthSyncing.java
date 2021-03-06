/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import tech.pegasys.pantheon.ethereum.core.Synchronizer;
import tech.pegasys.pantheon.ethereum.jsonrpc.RpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.SyncingResult;

/*
 * SyncProgress retrieves the current progress of the syncing algorithm. If there's no sync
 * currently running, it returns false.
 */
public class EthSyncing implements JsonRpcMethod {

  private final Synchronizer synchronizer;

  public EthSyncing(final Synchronizer synchronizer) {
    this.synchronizer = synchronizer;
  }

  @Override
  public String getName() {
    return RpcMethod.ETH_SYNCING.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    // Returns false when not synchronizing.
    final Object result =
        synchronizer.getSyncStatus().map(s -> (Object) new SyncingResult(s)).orElse(false);
    return new JsonRpcSuccessResponse(req.getId(), result);
  }
}
