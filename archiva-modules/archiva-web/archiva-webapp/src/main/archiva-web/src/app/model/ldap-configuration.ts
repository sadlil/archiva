/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

export class LdapConfiguration {
    host_name : string = "";
    port : number = 389;
    ssl_enabled : boolean  = false;
    context_factory: string = "";
    base_dn : string = "";
    groups_base_dn : string = "";
    bind_dn : string = "";
    bind_password : string = "";
    authentication_method : string = "";
    bind_authenticator_enabled : boolean = true;
    use_role_name_as_group : boolean = false;
    properties : Map<string,string> = new Map<string, string>()
    writable : boolean = false;
    available_context_factories : string[];
}
