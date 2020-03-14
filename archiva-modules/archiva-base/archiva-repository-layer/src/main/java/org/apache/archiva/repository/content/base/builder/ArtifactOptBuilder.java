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

package org.apache.archiva.repository.content.base.builder;

import org.apache.archiva.repository.content.ArtifactType;
import org.apache.archiva.repository.content.base.ArchivaArtifact;

/**
 * @author Martin Stockhammer <martin_s@apache.org>
 */
public interface ArtifactOptBuilder
    extends OptBuilder<ArchivaArtifact, ArtifactOptBuilder>
{

    ArtifactOptBuilder withArtifactVersion( String version );

    ArtifactOptBuilder withType( String type );

    ArtifactOptBuilder withClassifier( String classifier );

    ArtifactOptBuilder withRemainder( String remainder );

    ArtifactOptBuilder withContentType( String contentType );

    ArtifactOptBuilder withArtifactType( ArtifactType type );

    ArchivaArtifact build( );
}