/*
 * Copyright 2015-2018 MarkLogic Corporation
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
/**
 * A library that enables access to a MarkLogic-backed triple-store via the
 * RDF4J API.
 */
package com.marklogic.semantics.rdf4j.query;

import com.marklogic.client.FailedRequestException;
import com.marklogic.client.semantics.GraphPermissions;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.semantics.SPARQLRuleset;
import com.marklogic.semantics.rdf4j.MarkLogicRdf4jException;
import com.marklogic.semantics.rdf4j.client.MarkLogicClient;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.query.SPARQLQueryBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tuple query.
 *
 *
 */
public class MarkLogicTupleQuery extends MarkLogicQuery implements TupleQuery,MarkLogicQueryDependent {

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicTupleQuery.class);

    protected final long start = 1;
    protected final long pageLength=-1; // this value is a flag to not set setPageLength()

    /**
     * Constructor
     *
     * @param client
     * @param bindingSet
     * @param baseUri
     * @param queryString
     */
    public MarkLogicTupleQuery(MarkLogicClient client, SPARQLQueryBindingSet bindingSet, String baseUri, String queryString, GraphPermissions graphPerms, QueryDefinition queryDef, SPARQLRuleset[] rulesets) {
        super(client, bindingSet, baseUri, queryString, graphPerms, queryDef, rulesets);
    }

    /**
     * Evaluate tuple query. Note: take care to always close the TupleQueryResult after use to free any resources it keeps hold of.
     * It is recommended to use the try-with-resources pattern as TupleQueryResult implements AutoCloseable.
     *
     * @return TupleQueryResult
     * @throws QueryEvaluationException
     */
    @Override
    public TupleQueryResult evaluate() throws QueryEvaluationException {
        return evaluate(this.start,this.pageLength);
    }

    /**
     * Evaluate tuple query with pagination. Note: take care to always close the TupleQueryResult after use to free any resources it keeps hold of.
     * It is recommended to use the try-with-resources pattern as TupleQueryResult implements AutoCloseable.
     *
     * @param start
     * @param pageLength
     * @return TupleQueryResult
     * @throws QueryEvaluationException
     */
    public TupleQueryResult evaluate(long start, long pageLength)
            throws QueryEvaluationException {
        try {
            sync();
            return getMarkLogicClient().sendTupleQuery(getQueryString(), getBindings(), start, pageLength, getIncludeInferred(), getBaseURI());
        }catch (RepositoryException e) {
            throw new QueryEvaluationException(e.getMessage(), e);
        }catch (MalformedQueryException e) {
            throw new QueryEvaluationException(e.getMessage(), e);
        }catch(FailedRequestException e){
            throw new QueryEvaluationException(e.getMessage(), e);
        }
    }

    /**
     * Evaluate tuple query with TupleQueryResulthandler.
     *
     * @param resultHandler
     * @throws QueryEvaluationException
     * @throws TupleQueryResultHandlerException
     */
    @Override
    public void evaluate(TupleQueryResultHandler resultHandler) throws QueryEvaluationException, TupleQueryResultHandlerException {
        try {
            sync();
        } catch (MarkLogicRdf4jException e) {
            logger.info(e.getLocalizedMessage());
            throw new QueryEvaluationException(e);
        }
        TupleQueryResult queryResult = evaluate();
        if(queryResult.hasNext()) {
            QueryResults.report(queryResult, resultHandler);
        }
        queryResult.close();
    }

    /**
     * Evaluate tuple query with TupleQueryResulthandler and pagination.
     *
     * @param resultHandler
     * @throws QueryEvaluationException
     * @throws TupleQueryResultHandlerException
     */
    public void evaluate(TupleQueryResultHandler resultHandler,long start, long pageLength) throws QueryEvaluationException, TupleQueryResultHandlerException {
        TupleQueryResult queryResult = evaluate(start,pageLength);
        if(queryResult.hasNext()){
            QueryResults.report(queryResult, resultHandler);
        }
        queryResult.close();
    }
}
