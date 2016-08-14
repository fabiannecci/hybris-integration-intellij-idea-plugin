/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.type.system.validation;

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.type.system.common.TSMessages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.sun.istack.NotNull;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Vlad Bozhenok <vladbozhenok@gmail.com>
 */
public abstract class AbstractTSClassesValidation<T, M> {

    public String validateGeneratedClasses(@NotNull final List<T> xmlDefinedTypes,
                                         @NotNull final Collection<PsiClass> generatedClasses)
    {

        Assert.notNull(xmlDefinedTypes);
        Assert.notNull(generatedClasses);

        final List<PsiClass> filteredClasses = filterXmlTypesClasses(generatedClasses, xmlDefinedTypes);

        String validationMessage;

        for(final T xmlType: xmlDefinedTypes)
        {
            final PsiClass gneratedClass = getGeneratedClassForItem(xmlType, filteredClasses);

            if(null == gneratedClass)
            {
                return HybrisI18NBundleUtils.message(TSMessages.ErrorMessages.CLASS_NOT_GENERATED, buildItemName(xmlType));
            }
            validationMessage =  validateClass(xmlType, gneratedClass);
            if(StringUtils.isNotEmpty(validationMessage))
            {
                return validationMessage;
            }

        }
        return StringUtils.EMPTY;
    }

    /**
     * Takes all generated classes and filter only types defined in items.xml
     * @param classesToFilter
     * @param itemsToFind
     * @return
     */
    private List<PsiClass> filterXmlTypesClasses( @NotNull final Collection<PsiClass> classesToFilter,
                                                  @NotNull final List<T> itemsToFind)
    {
        Assert.notNull(classesToFilter);
        Assert.notNull(itemsToFind);

        String modelName;
        final List<PsiClass> filteredItemClasses = new ArrayList<>();
        for (final T item : itemsToFind)
        {
            for (final PsiClass psiClass : classesToFilter)
            {
                modelName = buildGeneratedClassName(item);
                if (psiClass.getName().endsWith(modelName))
                {
                    filteredItemClasses.add(psiClass);
                    break;
                }
            }
        }
        return filteredItemClasses;
    }

    private  String validateClass(final T xmlType, final PsiClass generatedClass)
    {

        final List<M> itemFields = getItemFields(xmlType);

        PsiField fieldToValidate;
        for(final M xmlField: itemFields)
        {
            fieldToValidate = getGeneratedFieldForAttribute(xmlField, generatedClass);

            if(null == fieldToValidate)
            {
                return HybrisI18NBundleUtils.message(TSMessages.ErrorMessages.FIELDS_NOT_GENERATED,
                                                     buildPropertyName(xmlField),
                                                     buildItemName(xmlType));
            }
            //todo: maybe add attribute type validation

        }
        return StringUtils.EMPTY;
    }

    /**
     * Finds field in generated class for attribute defined for type in items.xml
     */
    private  PsiField getGeneratedFieldForAttribute(final M field, final PsiClass generatedClass)
    {
        String filedName;
        for(final PsiField generatedField: generatedClass.getAllFields())
        {
            filedName = buildPropertyName(field);

            if(generatedField.getName().toLowerCase().endsWith(filedName.toLowerCase()))
            {
                return generatedField;
            }
        }
        return null;
    }

    /**
     * Finds generated class for type defined in items.xml
     */
    private PsiClass getGeneratedClassForItem(final T xmlType, final List<PsiClass> generatedClasses)
    {
        for(final PsiClass psiClass: generatedClasses)
        {
            if(psiClass.getName().endsWith(buildGeneratedClassName(xmlType)))
            {
                return psiClass;
            }
        }
        return null;
    }

    public abstract String buildGeneratedClassName(T item);

    public abstract String buildItemName(T item);

    public abstract String buildPropertyName(M property);

    public abstract List<M> getItemFields(@NotNull final T item);


}