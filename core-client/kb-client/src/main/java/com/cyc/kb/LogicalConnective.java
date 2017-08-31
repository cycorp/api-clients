package com.cyc.kb;

/*
 * #%L
 * File: LogicalConnective.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2017 Cycorp, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * The interface for logical connectives of the CycL language, such as
 * <code>#$and</code> and <code>#$or</code>. The salient property of these
 * relations is that they take {@link Sentence}s as arguments.
 *
 * @author baxter
 */
public interface LogicalConnective extends Relation {

}
