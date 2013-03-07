package org.apache.deltaspike.query.impl.builder.postprocessor;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Query;

import org.apache.deltaspike.query.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.query.impl.handler.JpaQueryPostProcessor;
import org.apache.deltaspike.query.impl.param.Parameters;
import org.apache.deltaspike.query.impl.util.QueryUtils;
import org.apache.deltaspike.query.impl.util.jpa.QueryStringExtractorFactory;


public class CountQueryPostProcessor implements JpaQueryPostProcessor {

    private static final Logger log = Logger.getLogger(CountQueryPostProcessor.class.getName());

    private final QueryStringExtractorFactory factory = new QueryStringExtractorFactory();

    @Override
    public Query postProcess(CdiQueryInvocationContext context, Query query) {
        String queryString = getQueryString(context, query);
        QueryExtraction extract = new QueryExtraction(queryString);
        String count = extract.rewriteToCount();
        log.log(Level.FINER, "Rewrote query {0} to {1}", new Object[] { queryString, count });
        Query result = context.getEntityManager().createQuery(count);
        Parameters params = context.getParams();
        params.applyTo(result);
        return result;
    }

    private String getQueryString(CdiQueryInvocationContext context, Query query) {
        if (QueryUtils.isNotEmpty(context.getQueryString())) {
            return context.getQueryString();
        }
        return factory.select(query).extractFrom(query);
    }

    private static class QueryExtraction {

        private String select;
        private String from;
        private String where;

        private String entityName;
        private final String query;

        public QueryExtraction(String query) {
            this.query = query;
        }

        public String rewriteToCount() {
            splitQuery();
            extractEntityName();
            return rewrite();
        }

        private String rewrite() {
            return "select count(" + (select != null ? select : entityName) + ") " + from + where;
        }

        private void extractEntityName() {
            String[] split = from.split(" ");
            if (split.length > 1) {
                entityName = split[split.length - 1];
            } else {
                entityName = "*";
            }
        }

        private void splitQuery() {
            String lower = query.toLowerCase();
            int selectIndex = lower.indexOf("select");
            int fromIndex = lower.indexOf("from");
            int whereIndex = lower.indexOf("where");
            if (selectIndex >= 0) {
                select = query.substring("select".length(), fromIndex);
            }
            if (whereIndex >= 0) {
                from = query.substring(fromIndex, whereIndex);
                where = query.substring(whereIndex);
            } else {
                from = query.substring(fromIndex);
            }
        }

    }

}