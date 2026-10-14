package ru.qacourse.shop.product;

import org.springframework.stereotype.Service;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;
import ru.qacourse.shop.common.ApiException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class ProductService {
    private final ProductRepository repo;
    private final BugRegistry bugs;

    public ProductService(ProductRepository repo, BugRegistry bugs) {
        this.repo = repo;
        this.bugs = bugs;
    }

    /**
     * Поиск с фильтрами, сортировкой и пагинацией.
     * Фильтрация сделана в памяти (товаров мало) — так логику проще читать на уроках.
     */
    public PageResponse<Product> search(String search, Category category, String sort, int page, int size) {
        if (page < 0) throw ApiException.badRequest("BAD_PAGE", "page не может быть отрицательным");
        if (size < 1 || size > 100) throw ApiException.badRequest("BAD_SIZE", "size должен быть от 1 до 100");

        Stream<Product> stream = repo.findAll().stream();
        if (search != null && !search.isBlank()) {
            if (bugs.isOn(Bug.BUG_103)) {
                stream = stream.filter(p -> p.getName().contains(search));
            } else {
                String q = search.toLowerCase(Locale.ROOT);
                stream = stream.filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(q)
                        || p.getDescription().toLowerCase(Locale.ROOT).contains(q));
            }
        }
        if (category != null && !bugs.isOn(Bug.BUG_105)) {
            stream = stream.filter(p -> p.getCategory() == category);
        }
        List<Product> all = stream.sorted(comparator(sort)).toList();

        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        if (bugs.isOn(Bug.BUG_101) && to - from > 1) {
            to = to - 1; // теряем последний элемент каждой страницы
        }
        int totalPages = (int) Math.ceil(all.size() / (double) size);
        return new PageResponse<>(all.subList(from, to), page, size, all.size(), totalPages);
    }

    private Comparator<Product> comparator(String sort) {
        if (sort == null || sort.isBlank()) return Comparator.comparing(Product::getId);
        String[] parts = sort.split(",");
        Comparator<Product> c = switch (parts[0]) {
            case "price" -> Comparator.comparing(Product::getPrice);
            case "name" -> Comparator.comparing(Product::getName);
            case "id" -> Comparator.comparing(Product::getId);
            default -> throw ApiException.badRequest("BAD_SORT", "Сортировка возможна по полям: id, name, price");
        };
        boolean desc = parts.length > 1 && parts[1].equalsIgnoreCase("desc");
        if (desc && !(bugs.isOn(Bug.BUG_102) && parts[0].equals("price"))) {
            c = c.reversed();
        }
        return c;
    }

    public Product get(long id) {
        if (bugs.isOn(Bug.BUG_104)) {
            Product p = repo.findById(id).orElse(null);
            p.getName(); // NullPointerException, если товара нет → 500
            return p;
        }
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("Товар " + id));
    }

    public Product create(Product p) {
        return repo.save(p);
    }

    public Product update(long id, Product changes) {
        Product p = get(id);
        p.setName(changes.getName());
        p.setDescription(changes.getDescription());
        p.setPrice(changes.getPrice());
        p.setCategory(changes.getCategory());
        p.setStock(changes.getStock());
        p.setEmoji(changes.getEmoji());
        return repo.save(p);
    }

    public void delete(long id) {
        repo.delete(get(id));
    }
}
