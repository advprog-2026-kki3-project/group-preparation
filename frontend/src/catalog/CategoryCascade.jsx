import React, { useMemo } from 'react';

export function CategoryCascade({
    categories,
    value,
    onChange,
    required = false,
    allLabel = "All Categories"
}) {
    const roots = useMemo(() => getRootCategories(categories), [categories]);
    const path = useMemo(() => findCategoryPath(roots, value), [roots, value]);
    const levels = useMemo(() => {
        const nextLevels = [roots];
        path.forEach((category) => {
            if (category.subCategories?.length) {
                nextLevels.push(sortCategories(category.subCategories));
            }
        });
        return nextLevels;
    }, [roots, path]);

    function handleLevelChange(levelIndex, selectedId) {
        if (selectedId) {
            onChange(selectedId);
            return;
        }

        onChange(levelIndex === 0 ? "" : path[levelIndex - 1].id);
    }

    return (
        <div className="category-cascade">
            {levels.map((levelCategories, levelIndex) => {
                const selectedAtLevel = path[levelIndex]?.id || "";
                const label = getLevelLabel(levelIndex);
                const isFirstLevel = levelIndex === 0;

                return (
                    <label key={levelIndex}>
                        {label}
                        <select
                            className="input"
                            required={required && isFirstLevel}
                            value={selectedAtLevel}
                            onChange={(event) => handleLevelChange(levelIndex, event.target.value)}
                        >
                            <option value="">
                                {isFirstLevel ? allLabel : `Any ${label.toLowerCase()}`}
                            </option>
                            {levelCategories.map((category) => (
                                <option key={category.id} value={category.id}>
                                    {category.name}
                                </option>
                            ))}
                        </select>
                    </label>
                );
            })}
        </div>
    );
}

function getRootCategories(categories) {
    const childIds = new Set();
    categories.forEach((category) => {
        collectChildIds(category, childIds);
    });

    return sortCategories(categories.filter((category) => !childIds.has(category.id)));
}

function collectChildIds(category, childIds) {
    category.subCategories?.forEach((child) => {
        childIds.add(child.id);
        collectChildIds(child, childIds);
    });
}

function findCategoryPath(categories, categoryId) {
    if (!categoryId) {
        return [];
    }

    for (const category of categories) {
        if (category.id === categoryId) {
            return [category];
        }

        const childPath = findCategoryPath(category.subCategories || [], categoryId);
        if (childPath.length) {
            return [category, ...childPath];
        }
    }

    return [];
}

function sortCategories(categories) {
    return [...categories].sort((first, second) => first.name.localeCompare(second.name));
}

function getLevelLabel(levelIndex) {
    if (levelIndex === 0) {
        return "Category";
    }
    if (levelIndex === 1) {
        return "Subcategory";
    }
    return "Type";
}
