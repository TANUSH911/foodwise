-- =====================================================================
-- Seed recipe catalogue. Ingredient names are lowercase and singular so
-- they match pantry items ("Tomatoes" in the pantry matches "tomato").
-- Salt, oil and spices are assumed and not listed.
-- Photos: TheMealDB (https://www.themealdb.com), loaded by the browser.
-- =====================================================================

INSERT INTO recipe (name, cuisine, prep_minutes, servings, difficulty, image_url, instructions) VALUES
('Shakshuka', 'Middle Eastern', 30, 2, 'Easy',
 'https://www.themealdb.com/images/media/meals/g373701551450225.jpg',
 'Saute sliced onion, capsicum and garlic in oil until soft. Add chopped tomatoes with cumin and paprika and simmer 10 minutes into a thick sauce. Make four wells, crack in the eggs, cover and cook until the whites set. Serve hot with bread.'),
('Fluffy Pancakes', 'American', 20, 4, 'Easy',
 'https://www.themealdb.com/images/media/meals/rwuyqx1511383174.jpg',
 'Whisk flour, a pinch of salt and a spoon of sugar. Beat in the egg and milk to a smooth batter, then stir in melted butter. Cook ladlefuls on a hot greased pan until bubbles appear, flip and cook one more minute.'),
('Banana Pancakes', 'American', 15, 2, 'Easy',
 'https://www.themealdb.com/images/media/meals/sywswr1511383814.jpg',
 'Mash ripe bananas, then whisk in the eggs and a splash of milk. Cook small rounds on a buttered pan over medium heat for about 2 minutes per side. A great way to use bananas with brown spots.'),
('Matar Paneer', 'Indian', 35, 3, 'Medium',
 'https://www.themealdb.com/images/media/meals/xxpqsy1511452222.jpg',
 'Fry onion and ginger until golden, add pureed tomatoes and spices and cook until oil separates. Add peas with a cup of water and simmer 8 minutes. Stir in paneer cubes and cream, cook 3 more minutes and finish with garam masala.'),
('Dal Fry', 'Indian', 40, 4, 'Easy',
 'https://www.themealdb.com/images/media/meals/wuxrtu1483564410.jpg',
 'Pressure cook rinsed lentils with turmeric until soft. Temper cumin in ghee, add garlic, ginger and onion, then tomato, and cook until mushy. Pour in the cooked lentils, adjust consistency with water and simmer 5 minutes.'),
('Chicken and Potato Curry', 'Indian', 50, 4, 'Medium',
 'https://www.themealdb.com/images/media/meals/9ya6o71780262651.jpg',
 'Marinate chicken in yogurt and spices for 15 minutes. Brown sliced onions, add tomatoes and cook down. Add the chicken and potato chunks with a cup of water, cover and simmer 30 minutes until the potatoes are tender.'),
('Spanish Tortilla', 'Spanish', 40, 4, 'Medium',
 'https://www.themealdb.com/images/media/meals/quuxsx1511476154.jpg',
 'Slowly fry thinly sliced potatoes and onion in plenty of oil until tender, then drain. Mix with beaten eggs and rest 10 minutes. Cook in a pan on low heat, flip with a plate and cook the other side until just set.'),
('Creamy Tomato Soup', 'European', 30, 4, 'Easy',
 'https://www.themealdb.com/images/media/meals/stpuws1511191310.jpg',
 'Soften onion and garlic in butter, add chopped tomatoes and two cups of water and simmer 15 minutes. Blend until smooth, stir in cream and season. Serve with toasted bread.'),
('Chicken Fried Rice', 'Chinese', 25, 3, 'Easy',
 'https://www.themealdb.com/images/media/meals/wuyd2h1765655837.jpg',
 'Stir-fry diced chicken until cooked and set aside. Scramble the eggs in the same wok, add carrot and peas, then the cold cooked rice. Return the chicken, season with soy sauce and toss on high heat for 2 minutes.'),
('Breakfast Potatoes', 'American', 30, 2, 'Easy',
 'https://www.themealdb.com/images/media/meals/1550441882.jpg',
 'Par-boil cubed potatoes for 5 minutes and drain. Fry in butter until golden, then add chopped onion and capsicum and cook until soft. Season with salt, pepper and paprika.'),
('Spinach and Cheese Cannelloni', 'Italian', 60, 4, 'Hard',
 'https://www.themealdb.com/images/media/meals/wspuvp1511303478.jpg',
 'Wilt the spinach, squeeze dry and mix with grated cheese. Fill the pasta tubes and lay them in a dish over a simple tomato sauce. Pour over a white sauce made with milk, top with more cheese and bake 30 minutes at 190 C.'),
('Carrot Soup', 'Moroccan', 35, 4, 'Easy',
 'https://www.themealdb.com/images/media/meals/jcr46d1614763831.jpg',
 'Saute onion and garlic, add sliced carrots with cumin and three cups of water. Simmer 20 minutes until soft, then blend smooth. Serve topped with a spoon of yogurt.');

INSERT INTO recipe_ingredient (recipe_id, ingredient_name, amount)
SELECT r.id, i.ingredient_name, i.amount
FROM recipe r
JOIN (
              SELECT 'Shakshuka' AS recipe_name, 'egg' AS ingredient_name, '4' AS amount
    UNION ALL SELECT 'Shakshuka', 'tomato', '4 large'
    UNION ALL SELECT 'Shakshuka', 'onion', '1'
    UNION ALL SELECT 'Shakshuka', 'capsicum', '1'
    UNION ALL SELECT 'Shakshuka', 'garlic', '3 cloves'
    UNION ALL SELECT 'Fluffy Pancakes', 'flour', '1 cup'
    UNION ALL SELECT 'Fluffy Pancakes', 'egg', '1'
    UNION ALL SELECT 'Fluffy Pancakes', 'milk', '1 cup'
    UNION ALL SELECT 'Fluffy Pancakes', 'butter', '2 tbsp'
    UNION ALL SELECT 'Banana Pancakes', 'banana', '2 ripe'
    UNION ALL SELECT 'Banana Pancakes', 'egg', '2'
    UNION ALL SELECT 'Banana Pancakes', 'milk', '1/4 cup'
    UNION ALL SELECT 'Matar Paneer', 'paneer', '200 g'
    UNION ALL SELECT 'Matar Paneer', 'pea', '1 cup'
    UNION ALL SELECT 'Matar Paneer', 'tomato', '2'
    UNION ALL SELECT 'Matar Paneer', 'onion', '1'
    UNION ALL SELECT 'Matar Paneer', 'cream', '2 tbsp'
    UNION ALL SELECT 'Matar Paneer', 'ginger', '1 inch'
    UNION ALL SELECT 'Dal Fry', 'lentil', '1 cup'
    UNION ALL SELECT 'Dal Fry', 'tomato', '1'
    UNION ALL SELECT 'Dal Fry', 'onion', '1'
    UNION ALL SELECT 'Dal Fry', 'garlic', '4 cloves'
    UNION ALL SELECT 'Dal Fry', 'ginger', '1 inch'
    UNION ALL SELECT 'Chicken and Potato Curry', 'chicken', '500 g'
    UNION ALL SELECT 'Chicken and Potato Curry', 'potato', '2'
    UNION ALL SELECT 'Chicken and Potato Curry', 'onion', '2'
    UNION ALL SELECT 'Chicken and Potato Curry', 'tomato', '2'
    UNION ALL SELECT 'Chicken and Potato Curry', 'yogurt', '1/2 cup'
    UNION ALL SELECT 'Spanish Tortilla', 'potato', '3'
    UNION ALL SELECT 'Spanish Tortilla', 'egg', '5'
    UNION ALL SELECT 'Spanish Tortilla', 'onion', '1'
    UNION ALL SELECT 'Creamy Tomato Soup', 'tomato', '6'
    UNION ALL SELECT 'Creamy Tomato Soup', 'onion', '1'
    UNION ALL SELECT 'Creamy Tomato Soup', 'garlic', '2 cloves'
    UNION ALL SELECT 'Creamy Tomato Soup', 'cream', '1/4 cup'
    UNION ALL SELECT 'Creamy Tomato Soup', 'bread', '4 slices'
    UNION ALL SELECT 'Chicken Fried Rice', 'rice', '2 cups cooked'
    UNION ALL SELECT 'Chicken Fried Rice', 'chicken', '200 g'
    UNION ALL SELECT 'Chicken Fried Rice', 'egg', '2'
    UNION ALL SELECT 'Chicken Fried Rice', 'carrot', '1'
    UNION ALL SELECT 'Chicken Fried Rice', 'pea', '1/2 cup'
    UNION ALL SELECT 'Breakfast Potatoes', 'potato', '3'
    UNION ALL SELECT 'Breakfast Potatoes', 'capsicum', '1'
    UNION ALL SELECT 'Breakfast Potatoes', 'onion', '1'
    UNION ALL SELECT 'Breakfast Potatoes', 'butter', '1 tbsp'
    UNION ALL SELECT 'Spinach and Cheese Cannelloni', 'spinach', '250 g'
    UNION ALL SELECT 'Spinach and Cheese Cannelloni', 'cheese', '200 g'
    UNION ALL SELECT 'Spinach and Cheese Cannelloni', 'pasta', '12 tubes'
    UNION ALL SELECT 'Spinach and Cheese Cannelloni', 'tomato', '4'
    UNION ALL SELECT 'Spinach and Cheese Cannelloni', 'milk', '1 cup'
    UNION ALL SELECT 'Carrot Soup', 'carrot', '6'
    UNION ALL SELECT 'Carrot Soup', 'onion', '1'
    UNION ALL SELECT 'Carrot Soup', 'garlic', '2 cloves'
    UNION ALL SELECT 'Carrot Soup', 'yogurt', '4 tbsp'
) i ON i.recipe_name = r.name;
