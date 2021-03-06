/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2.util;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.BdioObject;
import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;
import com.blackducksoftware.common.value.Product;
import com.blackducksoftware.common.value.ProductList;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.bdio2.model.Bdio2Document;

public class Bdio2Factory {
    public static final List<Product> DEFAULT_PRODUCTS = Arrays.asList(Product.java(), Product.os());

    public Bdio2Document createBdio2Document(final BdioMetadata bdioMetadata, final Project project, final DependencyGraph dependencyGraph) {
        final List<Component> components = createAndLinkComponents(dependencyGraph, project);
        return new Bdio2Document(bdioMetadata, project, components);
    }

    public BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime) {
        return createBdioMetadata(codeLocationName, creationDateTime, DEFAULT_PRODUCTS);
    }

    public BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime, Product product) {
        return createBdioMetadata(codeLocationName, creationDateTime, addLists(DEFAULT_PRODUCTS, Arrays.asList(product)));
    }

    public BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime, List<Product> products) {
        ProductList productList =
            addLists(DEFAULT_PRODUCTS, products)
                .stream()
                .collect(ProductList.toProductList());

        return createBdioMetadata(codeLocationName, creationDateTime, productList);
    }

    @Deprecated
    /**
     * deprecated Please use the other createBdioMetadata methods that ask for lists of Products, a single Product, or no Product at all.
     */
    public BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime, final ProductList.Builder productListBuilder) {
        DEFAULT_PRODUCTS
            .forEach(productListBuilder::addProduct);

        return createBdioMetadata(codeLocationName, creationDateTime, productListBuilder.build());
    }

    public Project createProject(final ExternalId projectExternalId, final String projectName, final String projectVersionName) {
        return new Project(projectExternalId.createBdioId().toString())
                   .identifier(projectExternalId.createExternalId())
                   .name(projectName)
                   .version(projectVersionName);
    }

    public List<Component> createAndLinkComponents(final DependencyGraph dependencyGraph, final Project project) {
        return createAndLinkComponentsFromGraph(dependencyGraph, project::dependency, dependencyGraph.getRootDependencies(), new HashMap<>());
    }

    private BdioMetadata createBdioMetadata(final String codeLocationName, final ZonedDateTime creationDateTime, ProductList productList) {
        return new BdioMetadata()
                   .id(LegacyUtilitiesClone.toNameUri(codeLocationName))
                   .name(codeLocationName)
                   .creationDateTime(creationDateTime)
                   .publisher(productList);
    }

    private List<Component> createAndLinkComponentsFromGraph(final DependencyGraph dependencyGraph, final DependencyFunction dependencyFunction, final Set<Dependency> dependencies, final Map<ExternalId, Component> existingComponents) {
        final List<Component> addedComponents = new ArrayList<>();

        for (final Dependency dependency : dependencies) {
            final Component component = componentFromDependency(dependency);
            dependencyFunction.dependency(new com.blackducksoftware.bdio2.model.Dependency().dependsOn(component));

            if (!existingComponents.containsKey(dependency.getExternalId())) {
                addedComponents.add(component);

                existingComponents.put(dependency.getExternalId(), component);
                final List<Component> children = createAndLinkComponentsFromGraph(dependencyGraph, component::dependency, dependencyGraph.getChildrenForParent(dependency), existingComponents);
                addedComponents.addAll(children);
            }
        }

        return addedComponents;
    }

    private Component componentFromDependency(final Dependency dependency) {
        return new Component(dependency.getExternalId().createBdioId().toString())
                   .name(dependency.getName())
                   .version(dependency.getVersion())
                   .identifier(dependency.getExternalId().createExternalId())
                   .namespace(dependency.getExternalId().getForge().getName());
    }

    private List<Product> addLists(List<Product> list1, List<Product> list2) {
        return Stream
                   .concat(list1.stream(), list2.stream())
                   .distinct()
                   .collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface DependencyFunction {
        BdioObject dependency(@Nullable com.blackducksoftware.bdio2.model.Dependency dependency);
    }

}
